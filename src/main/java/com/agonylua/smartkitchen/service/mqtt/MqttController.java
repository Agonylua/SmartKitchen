package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor // 自动为 final 字段生成构造函数
@Slf4j
public class MqttController {

    private final MessageChannel mqttOutputChannel;
    private final DeviceRepository deviceRepository;

    private static final String MQTT_TOPIC_PREFIX = "smartKitchen/";
    @Value("${mqtt.bind-topics}")
    private String bindTopic;
    @Value("${mqtt.publish-topics}")
    private String publishTopic;
    @Value("${mqtt.publish-qos}")
    private String publishQos;
    @Value("${mqtt.bind-qos}")
    private String bindQos;

    public void handleReceivedMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();

            if (topic.startsWith(MQTT_TOPIC_PREFIX)) {
                String[] topicParts = topic.substring(MQTT_TOPIC_PREFIX.length()).split("/");
                if (topicParts[0].isEmpty() || topicParts[1].isEmpty()) return;

                log.info("Received MQTT message - Topic: {}, Payload: {}", topic, payload);
                String sn = topicParts[1];
                if (topicParts[0].equals("device") && topicParts[2].equals("status")) {
                    deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                        if (payload.equals("offline")) {
                            device.setDeviceStatus(DeviceStatus.OFFLINE);
                        } else if (payload.equals("online")) {
                            device.setDeviceStatus(DeviceStatus.ONLINE);
                        }
                        deviceRepository.save(device);
                    }, () -> log.warn("收到绑定消息但设备不存在: {}", sn));
                }
                if (topicParts[0].equals("service")) {
                    if (topicParts[2].equals("update")) {
                        // 更新数据
                        deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                            if (payload.equals(device.getDeviceData())) {
                                log.debug("设备 {} 数据未变化，跳过更新", sn);
                                return;
                            }
                            device.setDeviceMode(JsonUtil.getValue(payload, "mode"));
                            device.setDeviceData(JsonUtil.getJsonStr(payload, "data"));
                            deviceRepository.save(device);
                            log.info("设备 {} 数据已更新", sn);

                        }, () -> log.warn("收到消息但设备不存在: {}", sn));
                    } else if (topicParts[2].equals("unbind")) {
                        deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                            device.setHomeId(null);
                            deviceRepository.save(device);
                        }, () -> log.warn("收到绑定消息但设备不存在: {}", sn));
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理MQTT接收消息时发生异常", e);
        }
    }

    // 发送消息
    public void sendBindMessage(String homeId, String deviceSn) {
        String topic = bindTopic + deviceSn + "/bindHomeId";
        toPayload(homeId, topic, bindQos);
    }

//    public void sendCmdMessage(String payload) {
//        String topic = publishTopic + JsonUtil.getValue(payload, "deviceSn") + "/control";
//        toPayload(payload, topic, publishQos);
//    }

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
}