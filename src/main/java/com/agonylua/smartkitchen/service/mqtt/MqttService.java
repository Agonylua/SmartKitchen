package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.service.RuleEngineService;
import com.agonylua.smartkitchen.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor // 自动为 final 字段生成构造函数
@Slf4j
public class MqttService {

    private static final String MQTT_TOPIC_PREFIX = "smartKitchen/";
    private final MessageChannel mqttOutputChannel;
    private final DeviceRepository deviceRepository;
    private final RuleEngineService ruleEngineService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Consumer<Boolean>> bindCallbackMap = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTaskMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService ruleEnginePool = Executors.newFixedThreadPool(4); // 规则引擎处理线程池
    @Getter
    private boolean connected = false;
    @Value("${mqtt.bind-topics}")
    private String bindTopic;
    @Value("${mqtt.publish-topics}")
    private String publishTopic;
    @Value("${mqtt.publish-qos}")
    private String publishQos;
    @Value("${mqtt.bind-qos}")
    private String bindQos;

    // 连接失败或掉线
    @EventListener
    public void handleMqttConnectionFailed(MqttConnectionFailedEvent event) {
        connected = false;
        log.warn("[MQTT服务] 连接异常或掉线: {}", event.getCause() != null ? event.getCause().getMessage() : "Unknown");
    }

    // 订阅成功代表已经连上（适配器重连成功时会重新订阅）
    @EventListener
    public void handleMqttSubscribed(MqttSubscribedEvent event) {
        connected = true;
        log.info("[MQTT服务] 已连接并准备就绪");
    }

    public void handleReceivedMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();

            if (topic.startsWith(MQTT_TOPIC_PREFIX)) {
                log.info("[MQTT服务] 处理消息 - Topic: {}, Payload: {}", topic, payload);
                String[] topicParts = topic.substring(MQTT_TOPIC_PREFIX.length()).split("/");
                if (topicParts[0].isEmpty() || topicParts[1].isEmpty()) return;
                String sn = topicParts[1];

                // 处理设备上下线
                if (topicParts[0].equals("status")) {
                    log.info("[MQTT服务] 设备状态变更");
                    deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                        device.setDeviceMode("IDLE");
                        device.setRunTime("0");
                        String statusVal = payload.equals("offline") ? "OFFLINE" : (payload.equals("online") ? "ONLINE" : "");
                        if (payload.equals("offline")) device.setDeviceStatus(DeviceStatus.OFFLINE);
                        else if (payload.equals("online")) device.setDeviceStatus(DeviceStatus.ONLINE);
                        deviceRepository.save(device);
                        // 触发规则引擎：设备状态改变
                        if (!statusVal.isEmpty()) {
                            ruleEnginePool.submit(() -> {
                                try {
                                    ruleEngineService.processDeviceEvent(sn, "status", statusVal);
                                } catch (Exception e) {
                                    log.error("[MQTT服务] 处理设备状态规则引擎事件时发生异常", e);
                                }
                            });
                        }
                    }, () -> log.warn("[MQTT服务] 收到状态消息但设备不存在: {}", sn));
                }

                if (topicParts[0].equals("sensor")) {
                    ruleEnginePool.submit(() -> {
                        try {
                            ruleEngineService.processDeviceEvent(sn, "sensor", payload);
                        } catch (Exception e) {
                            log.error("[MQTT服务] 处理设备状态规则引擎事件时发生异常", e);
                        }
                    });
                }

                // 处理设备数据更新与控制
                if (topicParts[0].equals("service")) {
                    switch (topicParts[2]) {
                        case "update" -> {
                            log.info("[MQTT服务] 设备数据更新 - SN: {}, Payload: {}", sn, payload);
                            deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                                if (payload.equals(device.getDeviceData())) return;

                                device.setDeviceMode(JsonUtil.getValue(payload, "mode"));
                                String newData = JsonUtil.getValue(payload, "data");
                                if (newData != null && newData.trim().startsWith("{")) {
                                    device.setDeviceData(newData);
                                }
                                String rawRunTime = JsonUtil.getValue(payload, "runTime");
                                if (rawRunTime != null) {
                                    String cleanRunTime = rawRunTime.replaceAll("[^0-9]", "");
                                    device.setRunTime(cleanRunTime.isEmpty() ? "0" : cleanRunTime);
                                }
                                deviceRepository.save(device);

                                // 触发规则引擎：解析具体的传感器数值并抛出事件
                                ruleEnginePool.submit(() -> {
                                    try {
                                        parseAndTriggerRules(sn, payload);
                                    } catch (Exception e) {
                                        log.error("处理设备规则引擎解析任务时发生异常", e);
                                    }
                                });

                            }, () -> log.warn("收到消息但设备不存在: {}", sn));
                        }
                        case "unbind" -> {
                            log.info("[MQTT服务] 设备解绑 - SN: {}, Payload: {}", sn, payload);
                            deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                                device.setHomeId(null);
                                deviceRepository.save(device);
                            }, () -> log.warn("收到绑定消息但设备不存在: {}", sn));
                        }
                        case "bind" -> {
                            log.info("[MQTT服务] 设备绑定 - SN: {}, Payload: {}", sn, payload);
                            Consumer<Boolean> callback = bindCallbackMap.remove(sn); // 取出并移除回调

                            ScheduledFuture<?> task = scheduledTaskMap.remove(sn);
                            if (task != null) {
                                task.cancel(false);
                            }
                            if (callback != null) {
                                if (payload.equals("1")) {
                                    CompletableFuture.runAsync(() -> callback.accept(true));
                                } else {
                                    CompletableFuture.runAsync(() -> callback.accept(false));
                                }
                            } else {
                                log.warn("[MQTT服务] 未找到设备 {} 的等待绑定回调任务 (可能已超时或未发起请求)", sn);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[MQTT服务] 处理MQTT接收消息时发生异常", e);
        }
    }

    /**
     * 将设备的 JSON 数据拆解为单一属性事件，扔给规则引擎
     */
    private void parseAndTriggerRules(String sn, String dataJson) {
        if (dataJson == null || !dataJson.trim().startsWith("{")) return;
        try {
            JsonNode dataNode = objectMapper.readTree(dataJson);
            dataNode.fieldNames().forEachRemaining(key -> {
                String value = dataNode.get(key).asText();
                ruleEngineService.processDeviceEvent(sn, key, value);
            });
        } catch (Exception e) {
            log.error("[MQTT服务] 解析设备数据用于规则引擎时出错", e);
        }
    }

    /**
     * 注册设备的异步绑定回调
     */
    public void registerBindCallback(String deviceSn, Consumer<Boolean> callback) {
        bindCallbackMap.put(deviceSn, callback);
        log.info("[MQTT服务] 已注册设备 {} 的绑定回调，等待硬件端 MQTT 消息...", deviceSn);

        ScheduledFuture<?> existingTask = scheduledTaskMap.get(deviceSn);
        if (existingTask != null) {
            existingTask.cancel(false);
        }

        // 设置 5 分钟超时
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            if (bindCallbackMap.remove(deviceSn) != null) {
                log.warn("[MQTT服务] 设备 {} 配网绑定等待超时，清理回调缓存", deviceSn);
                //sendReset(deviceSn);
            }
            scheduledTaskMap.remove(deviceSn);
        }, 5, TimeUnit.MINUTES);

        scheduledTaskMap.put(deviceSn, task);
    }

    // 发送消息
    public void sendBind(String homeId, String deviceSn) {
        log.info("[MQTT服务] 发送设备绑定消息 - SN: {}, HomeID: {}", deviceSn, homeId);
        String topic = bindTopic + deviceSn + "/bind";
        if (homeId != null && !homeId.isEmpty()) {
            toPayload(homeId, topic, bindQos);
        }
    }

    public void sendUnBind(String deviceSn) {
        log.info("[MQTT服务] 发送设备解绑消息 - SN: {}", deviceSn);
        String topic = bindTopic + deviceSn + "/unBind";
        toPayload("", topic, bindQos);
    }

    public void sendCmdMessage(Map<String, String> payload) {
        log.info("[MQTT服务] 发送设备控制消息 - SN: {}, Payload: {}", payload.get("deviceSn"), payload);
        String topic = publishTopic + payload.get("deviceSn") + "/control";
        payload.remove("deviceSn");
        toPayload(JsonUtil.mapToJson(payload), topic, publishQos);
    }

    private void toPayload(String payload, String topic, String publishQos) {
        log.info("[MQTT服务] 消息载荷封装 - Topic: {}, Payload: {}, QoS: {}", topic, payload, publishQos);
        Message<String> message = null;
        try {
            int qos = Integer.parseInt(publishQos);
            message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .setHeader(MqttHeaders.QOS, qos)
                    .build();
        } catch (NumberFormatException e) {
            log.error("Invalid QoS value: {}", publishQos, e);
        }
        if (message != null) {
            try {
                mqttOutputChannel.send(message);
            } catch (Exception e) {
                log.error("[MQTT服务] 发送MQTT消息失败", e);
                throw e;
            }
        }
    }

    /**
     * Spring Boot 容器销毁时优雅停机，释放线程池资源
     */
    @PreDestroy
    public void onDestroy() {
        log.info("[MQTT服务] 正在关闭，释放资源...");
        ruleEnginePool.shutdown();
        scheduler.shutdown();
    }
}
