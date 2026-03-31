package com.agonylua.smartkitchen.service.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String brokerUrl;

    @Value("${mqtt.clientId_in}")
    private String clientId_in;

    @Value("${mqtt.clientId_out}")
    private String clientId_out;

    @Value("${mqtt.subscribe-topics}")
    private String[] subscribeTopics;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;


    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(30000);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId_in, mqttClientFactory(),
                        subscribeTopics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    //接收消息
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(MqttService mqttService) {
        return mqttService::handleReceivedMessage;
    }

    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    //发送消息
    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId_out, mqttClientFactory());
        messageHandler.setAsync(true);
        return messageHandler;
    }
}