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
        MqttConnectOptions options = new MqttConnectOptions(); // 创建连接选项对象
        options.setServerURIs(new String[]{brokerUrl}); // 设置 MQTT 服务器地址
        options.setAutomaticReconnect(true); // 自动重连
        options.setMaxReconnectDelay(30000); // 最大重连间隔 30 秒
        options.setCleanSession(false); // 保持会话，重连后继续接收消息
        options.setConnectionTimeout(10); // 连接超时 10 秒
        options.setKeepAliveInterval(20); // 心跳间隔 20 秒
        options.setUserName(username); // 设置认证用户名
        options.setPassword(password.toCharArray()); // 设置认证密码
        factory.setConnectionOptions(options); // 设置连接选项
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
        adapter.setCompletionTimeout(5000); // 设置消息处理完成的超时时间为 5 秒
        adapter.setConverter(new DefaultPahoMessageConverter()); // 使用默认的消息转换器，将 MQTT 消息转换为 Spring Integration 消息
        adapter.setQos(1); // 设置 QoS 级别为 1，确保消息至少被处理一次
        adapter.setOutputChannel(mqttInputChannel()); // 设置消息发送到 mqttInputChannel 进行处理
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
        messageHandler.setAsync(true); // 异步发送消息，避免阻塞调用线程
        return messageHandler;
    }
}