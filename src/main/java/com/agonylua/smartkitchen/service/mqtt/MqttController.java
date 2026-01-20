package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttController {

    @Autowired
    private MessageChannel mqttOutputChannel;

    @Value("${mqtt.publish-topics}")
    private String publishTopic;
    private static final String MQTT_TOPIC_PREFIX = "smartKitchen/service/";
    private final DeviceRepository deviceRepository;

    public void handleReceivedMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();

            if (topic.startsWith(MQTT_TOPIC_PREFIX)) {
                String sn = topic.substring(MQTT_TOPIC_PREFIX.length());
                if (!sn.isEmpty()) {
                    deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                        device.setDeviceData(payload);
                        deviceRepository.save(device);
                    }, () -> log.warn("收到消息但设备不存在: {}", sn));
                }
            }
        } catch (Exception e) {
            log.error("处理MQTT接收消息时发生异常", e);
        }
    }

    // 发送消息
    public void sendMessage(Object payload) {
        String json = JsonUtil.toJson(payload);
        String topic = publishTopic + JsonUtil.getValue(json, "deviceSn");
        String cmd = JsonUtil.getValue(json, "deviceData");
        Message<String> message = null;
        if (json != null) {
            message = MessageBuilder
                    .withPayload(cmd)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .setHeader(MqttHeaders.QOS, 1)
                    .build();
        }
        if (message != null) {
            mqttOutputChannel.send(message);
            System.out.println("发送内容：" + message);
        }
    }
}