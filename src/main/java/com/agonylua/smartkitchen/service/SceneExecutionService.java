package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.entity.DeviceMode;
import com.agonylua.smartkitchen.databases.entity.DeviceType;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.service.mqtt.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SceneExecutionService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final String CMD_MODE = "mode";
    private final String CMD_STATUS = "status";
    @Autowired
    private MqttService mqttService;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private HomeRepository homeRepository;

    /**
     * Android App 调用此接口触发一键场景
     */
    public Boolean executeManualScene(String UserId, String ruleMode) {
        String homeId = homeRepository.findByOwnerIdOrMemberIds(UserId).map(home -> home.getHomeId()).orElse(null);
        log.info("[预设场景] 开始执行家庭 {} 的场景: {}", homeId, ruleMode);
        List<Device> homeDevices = deviceRepository.findByHomeId(homeId);

        switch (ruleMode) {
            case "SCENE_LEAVE_HOME": // 离家安全
                log.info("[预设场景] 执行离家安全场景: 家庭 {} 设备数量 {}", homeId, homeDevices.size());
                homeDevices.forEach(device -> {
                    if (device.getDeviceType().equals(DeviceType.REFRIGERATOR)) {
                        sendCommandAsync(device.getDeviceSn(), CMD_MODE, DeviceMode.HOLIDAY.name());
                        deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                            updatedDevice.setDeviceMode(DeviceMode.HOLIDAY.name());
                            deviceRepository.save(updatedDevice);
                        });
                    } else {
                        sendCommandAsync(device.getDeviceSn(), CMD_MODE, DeviceMode.IDLE.name());
                        deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                            updatedDevice.setDeviceMode(DeviceMode.IDLE.name());
                            deviceRepository.save(updatedDevice);
                        });
                    }
                });
                break;

            case "SCENE_CLEANUP": // 饭后清洁
                log.info("[预设场景] 执行饭后清洁场景: 家庭 {} 设备数量 {}", homeId, homeDevices.size());
                for (Device device : homeDevices) {
                    if (device.getDeviceType().equals(DeviceType.DISHWASHER)) {
                        sendCommandAsync(device.getDeviceSn(), CMD_MODE, DeviceMode.INTENSIVE_WASH.name());
                        deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                            updatedDevice.setDeviceMode(DeviceMode.INTENSIVE_WASH.name());
                            deviceRepository.save(updatedDevice);
                        });
                        scheduler.schedule(() -> {
                            for (Device device1 : homeDevices) {
                                if (device1.getDeviceType().equals(DeviceType.STERILIZER)) {
                                    sendCommandAsync(device1.getDeviceSn(), CMD_MODE, DeviceMode.UVB.name());
                                    deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                                        updatedDevice.setDeviceMode(DeviceMode.UVB.name());
                                        deviceRepository.save(updatedDevice);
                                    });
                                }
                            }
                        }, 30, TimeUnit.MINUTES);
                    }
                }
                break;
            case "SCENE_MEALPREP": // 备餐模式
                log.info("[预设场景] 执行备餐模式场景: 家庭 {} 设备数量 {}", homeId, homeDevices.size());
                for (Device device : homeDevices) {
                    if (device.getDeviceType().equals(DeviceType.MICROWAVE)) {
                        sendCommandAsync(device.getDeviceSn(), CMD_MODE, DeviceMode.IDLE.name());
                        deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                            updatedDevice.setDeviceMode(DeviceMode.IDLE.name());
                            deviceRepository.save(updatedDevice);
                        });
                    } else if (device.getDeviceType().equals(DeviceType.RICE_COOKER)) {
                        sendCommandAsync(device.getDeviceSn(), CMD_MODE, DeviceMode.COOK_RICE.name());
                        deviceRepository.findByDeviceSn(device.getDeviceSn()).ifPresent(updatedDevice -> {
                            updatedDevice.setDeviceMode(DeviceMode.COOK_RICE.name());
                            deviceRepository.save(updatedDevice);
                        });
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    // 异步发送 MQTT 指令
    @Async("taskExecutor")
    public void sendCommandAsync(String deviceSn, String cmd, String mode) {
        Map<String, String> command = new HashMap<>();
        command.put("deviceSn", deviceSn);
        command.put(cmd, mode);
        mqttService.sendCmdMessage(command);
    }
}