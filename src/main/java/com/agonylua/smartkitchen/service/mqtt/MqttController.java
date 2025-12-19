package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.utils.JsonUtils;
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
        String payload = message.getPayload().toString();
        String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();
        topic = topic.startsWith(MQTT_TOPIC_PREFIX)
                ? topic.substring(MQTT_TOPIC_PREFIX.length())
                : "";
        Device device = deviceRepository.findByDeviceSn(topic)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        device.setDeviceData(payload);
        deviceRepository.save(device);
    }

    // 发送消息
    public void sendMessage(Object payload) {
        String json = JsonUtils.toJson(payload);
        String topic = publishTopic + JsonUtils.getValue(json, "device_sn");
        Message<String> message = null;
        if (json != null) {
            message = MessageBuilder
                    .withPayload(json)
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