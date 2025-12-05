package com.agonylua.smartkitchen.service.mqtt;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MqttGateway {

    private final MessageChannel mqttOutputChannel;

    /**
     * 发送到指定 topic
     * @param topic 目标 topic
     * @param payload 消息内容
     */
    public void send(String topic, String payload) {
        mqttOutputChannel.send(
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(MqttHeaders.TOPIC, topic)
                        .setHeader(MqttHeaders.QOS, 1)
                        .build()
        );
    }

    /**
     * 发送到默认 topic
     * @param payload 消息内容
     */
    public void send(String payload) {
        mqttOutputChannel.send(MessageBuilder.withPayload(payload).build());
    }
}