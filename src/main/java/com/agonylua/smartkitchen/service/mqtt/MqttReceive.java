package com.agonylua.smartkitchen.service.mqtt;

import com.alibaba.fastjson2.JSON;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class MqttReceive implements MessageHandler {

        private final DeviceRepository deviceRepository;

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            String payload = message.getPayload().toString();

            log.info("收到 MQTT => topic: {}, payload: {}", topic, payload);

            if (topic != null && topic.contains("/report")) {
                String number = topic.split("/")[1];

                deviceRepository.findByNumber(number).ifPresentOrElse(device -> {
                    try {
                        var json = JSON.parseObject(payload);
                        device.setStatus(json.getString("status"));
                        device.setLast_heartbeat(LocalDateTime.now());
                        deviceRepository.save(device);
                        log.info("设备 {} 上报成功，已更新数据库", number);
                    } catch (Exception e) {
                        log.warn("解析上报数据失败: {}", payload);
                    }
                }, () -> log.warn("收到未知设备上报: {}", number));
            }
        }
    }