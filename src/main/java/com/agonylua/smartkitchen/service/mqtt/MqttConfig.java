package com.agonylua.smartkitchen.service.mqtt;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConfig {

    @Value("${mqtt.url}")
    private String brokerUrl;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.default-topic}")
    private String defaultTopic;


    /**
     * MQTT 客户端工厂
     * @return 客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);  // 文章建议：clean session
        options.setConnectionTimeout(30);
        options.setAutomaticReconnect(true);  // 文章扩展：自动重连
        if (!username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * 输入通道（接收消息）
     * @return 消息通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 消息适配器（订阅主题并接收消息）
     * @param mqttClientFactory 客户端工厂
     * @return 消息适配器
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory,
                        "device/+/report",  // 多主题订阅（+ 通配符）
                        "device/+/status",
                        defaultTopic);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());  // 文章关键：消息转换器
        adapter.setQos(1);  // 文章推荐：至少一次交付
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 消息处理器
     * @return 消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
                String payload = message.getPayload().toString();

                log.info("收到 MQTT 消息: topic={}, payload={}", topic, payload);

                // 文章示例：简单打印
                System.out.println("Received: " + payload);
            }
        };
    }

    /**
     * 输出通道（发布消息）
     * @return 消息通道
     */
    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    /**
     * 发布处理器
     * @param mqttClientFactory 客户端工厂
     * @return 消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientId + "-publisher", mqttClientFactory);
        handler.setAsync(true);  // 异步发送
        handler.setDefaultQos(1);
        handler.setDefaultTopic(defaultTopic);  // 默认主题
        return handler;
    }
}