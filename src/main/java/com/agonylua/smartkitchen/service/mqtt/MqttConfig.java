package com.agonylua.smartkitchen.service.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id-prefix}")
    private String clientIdPrefix;

    @Value("${mqtt.username:#{null}}")
    private String username;

    @Value("${mqtt.password:#{null}}")
    private String password;

    @Bean
    public MqttPahoClientFactory clientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        if (username != null) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        factory.setConnectionOptions(options);
        return factory;
    }

    // 用于发送消息
    @Bean
    public MessageHandler mqttOutbound(MqttPahoClientFactory clientFactory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientIdPrefix + "-out-" + System.currentTimeMillis(), clientFactory);
        handler.setAsync(true);
        handler.setDefaultQos(1);
        return handler;
    }
}