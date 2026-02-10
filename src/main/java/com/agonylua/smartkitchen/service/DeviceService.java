package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import com.agonylua.smartkitchen.databases.entity.DeviceType;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.service.mqtt.MqttController;
import com.agonylua.smartkitchen.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private MqttController mqttController;

    public void addDevice(String homeId, String customName, String typeStr) {
        Device device = new Device();

        // 生成 12位 SN
        device.setDeviceSn(IdUtil.generateDeviceSn());

        device.setHomeId(homeId);
        device.setDeviceName(customName);

        // 校验设备类型 (如果输入值不在枚举中，会抛出异常)
        try {
            DeviceType type = DeviceType.valueOf(typeStr); // 自动校验
            device.setDeviceType(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("设备类型不支持");
        }
        // 默认状态为离线
        DeviceStatus status = DeviceStatus.OFFLINE;
        device.setDeviceStatus(status);

        deviceRepository.save(device);
    }

    public Integer bindDevice(String deviceSn, String homeId) {
        AtomicInteger result = new AtomicInteger(2);
        log.info("尝试绑定设备 {} 到家庭 {}", deviceSn, homeId);
        // 查找设备并更新 homeId
        deviceRepository.findByDeviceSn(deviceSn).ifPresentOrElse(device -> {
            if (homeId == null || homeId.trim().isEmpty()) {
                log.warn("绑定失败，家庭ID无效: {}", homeId);
                result.set(-1);
                return;
            }
            if (device.getHomeId() == null) {
                log.info("设备 {} 之前未绑定，现在绑定到家庭 {}", deviceSn, homeId);
                device.setHomeId(homeId);
                deviceRepository.save(device);
                result.set(1);
            } else {
                if (!device.getHomeId().equals(homeId)) {
                    log.warn("设备 {} 已绑定到家庭 {}, 现在绑定到家庭 {}", deviceSn, device.getHomeId(), homeId);
                    device.setHomeId(homeId);
                    deviceRepository.save(device);
                    result.set(1);
                } else {
                    log.info("设备 {} 已经绑定到家庭 {}, 无需重复绑定", deviceSn, homeId);
                    result.set(0);
                }
            }
            log.info("设备已经绑定成功");
        }, () -> {
            log.warn("绑定失败，设备不存在: {}", deviceSn);
            result.set(-1);
        });
        return result.get();
    }
}