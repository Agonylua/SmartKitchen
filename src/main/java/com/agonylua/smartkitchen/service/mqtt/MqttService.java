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
    // 注入规则引擎与 JSON 解析器
    private final RuleEngineService ruleEngineService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Consumer<Boolean>> bindCallbackMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // 🚀 新增：专门用于异步执行自动化规则计算的线程池
    private final ExecutorService ruleEnginePool = Executors.newFixedThreadPool(4);
    /**
     * -- GETTER --
     * 获取当前 MQTT 连接状态
     *
     * @return true 为已连接, false 为失去连接
     */
    // 记录 MQTT 连接状态
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
        log.warn("MQTT服务连接异常或掉线: {}", event.getCause() != null ? event.getCause().getMessage() : "Unknown");
    }

    // 订阅成功代表已经连上（适配器重连成功时会重新订阅）
    @EventListener
    public void handleMqttSubscribed(MqttSubscribedEvent event) {
        connected = true;
        log.info("MQTT服务已连接并准备就绪");
    }

    public void handleReceivedMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();

            if (topic.startsWith(MQTT_TOPIC_PREFIX)) {
                String[] topicParts = topic.substring(MQTT_TOPIC_PREFIX.length()).split("/");
                if (topicParts[0].isEmpty() || topicParts[1].isEmpty()) return;

                log.info("Received MQTT message - Topic: {}, Payload: {}", topic, payload);
                String sn = topicParts[1];

                // 1. 处理设备上下线
                if (topicParts[0].equals("status")) {
                    deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                        String statusVal = payload.equals("offline") ? "OFFLINE" : (payload.equals("online") ? "ONLINE" : "");
                        if (payload.equals("offline")) device.setDeviceStatus(DeviceStatus.OFFLINE);
                        else if (payload.equals("online")) device.setDeviceStatus(DeviceStatus.ONLINE);
                        deviceRepository.save(device);
                        // 触发规则引擎：设备状态改变
                        if (!statusVal.isEmpty()) {
                            log.info("设备 {} 状态更新为: {}", sn, statusVal);
                            ruleEnginePool.submit(() -> {
                                try {
                                    ruleEngineService.processDeviceEvent(sn, "status", statusVal);
                                } catch (Exception e) {
                                    log.error("处理设备状态规则引擎事件时发生异常", e);
                                }
                            });
                        }
                    }, () -> log.warn("收到状态消息但设备不存在: {}", sn));
                }

                // 2. 处理设备数据更新与控制
                if (topicParts[0].equals("service")) {
                    if (topicParts[2].equals("update")) {
                        deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                            if (payload.equals(device.getDeviceData())) return;

                            device.setDeviceMode(JsonUtil.getValue(payload, "mode"));
                            String newData = JsonUtil.getJsonStr(payload, "data");
                            if (newData != null && newData.trim().startsWith("{")) {
                                device.setDeviceData(newData);
                            }
                            device.setRunTime(JsonUtil.getJsonStr(payload, "runTime"));
                            deviceRepository.save(device);

                            // 🚀 触发规则引擎：解析具体的传感器数值并抛出事件
                            ruleEnginePool.submit(() -> {
                                try {
                                    parseAndTriggerRules(sn, newData);
                                } catch (Exception e) {
                                    log.error("处理设备规则引擎解析任务时发生异常", e);
                                }
                            });

                        }, () -> log.warn("收到消息但设备不存在: {}", sn));
                    } else if (topicParts[2].equals("unbind")) {
                        deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                            device.setHomeId(null);
                            deviceRepository.save(device);
                        }, () -> log.warn("收到绑定消息但设备不存在: {}", sn));
                    } else if (topicParts[2].equals("bind")) {
                        log.info("收到设备 {} 的配网绑定消息: {}", sn, payload);
                        Consumer<Boolean> callback = bindCallbackMap.remove(sn); // 取出并移除回调
                        if (callback != null) {
                            if (payload.equals("1")) {
                                CompletableFuture.runAsync(() -> callback.accept(true));
                            } else {
                                CompletableFuture.runAsync(() -> callback.accept(false));
                            }
                        } else {
                            log.warn("未找到设备 {} 的等待绑定回调任务 (可能已超时或未发起请求)", sn);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理MQTT接收消息时发生异常", e);
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
                ruleEngineService.processDeviceEvent(sn, key, value); // 例如: processDeviceEvent("esp32_1", "temperature", "32.5")
            });
        } catch (Exception e) {
            log.error("解析设备数据用于规则引擎时出错", e);
        }
    }

    /**
     * 注册设备的异步绑定回调
     */
    public void registerBindCallback(String deviceSn, Consumer<Boolean> callback) {
        bindCallbackMap.put(deviceSn, callback);
        log.info("已注册设备 {} 的绑定回调，等待硬件端 MQTT 消息...", deviceSn);

        // 设置 3 分钟超时
        scheduler.schedule(() -> {
            if (bindCallbackMap.remove(deviceSn) != null) {
                log.warn("⏳ 设备 {} 配网绑定等待超时，清理回调缓存", deviceSn);
            }
        }, 3, TimeUnit.MINUTES);
    }

    // 发送消息
    public void sendBind(String homeId, String deviceSn) {
        String topic = bindTopic + deviceSn + "/bindHomeId";
        if (homeId != null && !homeId.isEmpty()) {
            toPayload(homeId, topic, bindQos);
        }
    }

    public void sendUnBind(String homeId, String deviceSn) {
        String topic = bindTopic + deviceSn + "/bindHomeId";
        if (homeId != null && !homeId.isEmpty()) {
            toPayload(homeId, topic, bindQos);
        }
    }

    public void sendCmdMessage(Map<String, String> payload) {
        String topic = publishTopic + payload.get("deviceSn") + "/control";
        payload.remove("deviceSn");
        log.info("准备发送 MQTT 控制消息 - 设备: {}, Payload: {}", topic, payload);
        toPayload(JsonUtil.mapToJson(payload), topic, publishQos);
    }

    private void toPayload(String payload, String topic, String publishQos) {
        Message<String> message = null;
        if (payload != null) {
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
        }
        if (message != null) {
            try {
                mqttOutputChannel.send(message);
                log.info("Sent MQTT message: {}", message);
            } catch (Exception e) {
                log.error("Failed to send MQTT message", e);
                throw e;
            }
        }
    }

    /**
     * Spring Boot 容器销毁时优雅停机，释放线程池资源
     */
    @PreDestroy
    public void onDestroy() {
        log.info("准备关闭 MqttService 线程池资源...");
        ruleEnginePool.shutdown();
        scheduler.shutdown();
    }
}