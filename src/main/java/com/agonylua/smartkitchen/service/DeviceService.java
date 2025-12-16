package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.entity.DeviceType;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.utils.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public void addDevice(String homeId, String customName, String typeStr) {
        Device device = new Device();

        // 1. 生成 12位 SN
        device.setDeviceSn(IdUtils.generateDeviceSn());

        device.setHomeId(homeId);
        device.setDeviceName(customName);

        // 2. 校验设备类型 (如果输入值不在枚举中，会抛出异常)
        try {
            DeviceType type = DeviceType.valueOf(typeStr); // 自动校验
            device.setDeviceType(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("设备类型不支持，仅支持：电磁炉、冰箱、微波炉...");
        }

        deviceRepository.save(device);
    }
}