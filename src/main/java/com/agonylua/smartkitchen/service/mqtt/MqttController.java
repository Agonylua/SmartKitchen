package com.agonylua.smartkitchen.service.mqtt;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttController {

    @Autowired
    private MessageChannel mqttOutputChannel;

    @Value("${mqtt.publish-topics}")
    private String publishTopic;

    public void handleReceivedMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        System.out.println(payload);
    }

    // 发送消息
    public void sendMessage(Object payload) {
        String json = JsonUtils.toJson(payload);
        String topic = publishTopic + JsonUtils.getValue(json, "device_id");
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
        }
    }
}