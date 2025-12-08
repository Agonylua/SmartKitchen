package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void handleReceivedMessage(Message<?> message) {
    }

    // 发送消息
    public void sendMessage(String topic, Object payload) {
        String json = JsonUtils.toJson(payload);
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

    private void handleMessageProcessing(String r) {
        if (r == null) {
            log.warn("R is null");
            return;
        }
        switch (r) {
            case "#000":
                log.info("处理心跳消息");
                break;
            case "#001":
                log.info("处理设备状态更新消息");
                break;
            case "#002":
                log.info("处理控制命令响应消息");
                break;
            default:
                log.warn("未知事件: {}", r);
                break;
        }
    }
}