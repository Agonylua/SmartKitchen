package com.agonylua.smartkitchen.service.mqtt;

import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor // 自动为 final 字段生成构造函数
@Slf4j
public class MqttController {

    private final MessageChannel mqttOutputChannel;
    private final DeviceRepository deviceRepository;

    @Value("${mqtt.return-topics}")
    private String returnTopic;
    @Value("${mqtt.return-qos}")
    private String returnQos;
    @Value("${mqtt.publish-topics}")
    private String publishTopic;
    @Value("${mqtt.publish-qos}")
    private String publishQos;

    private static final String MQTT_TOPIC_PREFIX = "smartKitchen/service/";

    public void handleReceivedMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();

            if (topic.startsWith(MQTT_TOPIC_PREFIX)) {
                String[] topicParts = topic.substring(MQTT_TOPIC_PREFIX.length()).split("/");
                if (topicParts[0].isEmpty() || topicParts[1].isEmpty()) return;

                String sn = topicParts[0];
                if (topicParts[1].equals("update")) {
                    // 更新数据
                    deviceRepository.findByDeviceSn(sn).ifPresentOrElse(device -> {
                        if (payload.equals(device.getDeviceData())) {
                            log.debug("设备 {} 数据未变化，跳过更新", sn);
                            return;
                        }

                        device.setDeviceData(payload);
                        deviceRepository.save(device);
                        log.info("设备 {} 数据已更新", sn);

                    }, () -> log.warn("收到消息但设备不存在: {}", sn));
                } else if (topicParts[1].equals("return")) {
                    // 设备返回消息处理
                    sendReturnMessage(payload);
                    log.info("收到设备 {} 的返回消息: {}", sn, payload);

                }
            }
        } catch (Exception e) {
            log.error("处理MQTT接收消息时发生异常", e);
        }
    }

    // 发送消息
    public void sendReturnMessage(Object payload) {
        String topic = returnTopic + JsonUtil.getValue(JsonUtil.toJson(payload), "deviceSn") + "/return";
        toPayload(payload, topic, returnQos);
    }

    public void sendCmdMessage(String payload) {
        String topic = publishTopic + JsonUtil.getValue(payload, "deviceSn") + "/control";
        toPayload(payload, topic, publishQos);
    }

    private void toPayload(Object payload, String topic, String publishQos) {
        String cmd = JsonUtil.getValue(JsonUtil.toJson(payload), "deviceData");
        Message<String> message = null;
        if (JsonUtil.toJson(payload) != null) {
            message = MessageBuilder
                    .withPayload(cmd)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .setHeader(MqttHeaders.QOS, publishQos)
                    .build();
        }
        if (message != null) {
            mqttOutputChannel.send(message);
            System.out.println("发送内容：" + message);
        }
    }
}