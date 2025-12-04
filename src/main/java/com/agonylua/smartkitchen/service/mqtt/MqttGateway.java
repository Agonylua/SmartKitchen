package com.agonylua.smartkitchen.service.mqtt;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.messaging.MessageHandler;

@Component
@RequiredArgsConstructor
public class MqttGateway {

    private final MessageHandler mqttOutbound;

    public void send(String topic, String payload) {
        mqttOutbound.handleMessage(
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(MqttHeaders.TOPIC, topic)
                        .setHeader(MqttHeaders.QOS, 1)
                        .build()
        );
    }
}