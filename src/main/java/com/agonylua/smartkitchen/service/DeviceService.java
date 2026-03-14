package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import com.agonylua.smartkitchen.databases.entity.DeviceType;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.service.mqtt.MqttController;
import com.agonylua.smartkitchen.utils.IdUtil;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 发起设备绑定验证
     *
     * @return -1: 验证失败, 0: 已绑定, 1: 验证通过等待硬件确认
     */
    public Integer bindDevice(String deviceSn, String homeId) {
        log.info("开始验证设备绑定请求: 设备 {} , 家庭 {}", deviceSn, homeId);

        if (homeId == null || homeId.trim().isEmpty()) {
            log.warn("绑定验证失败，家庭ID无效: {}", homeId);
            return -1;
        }

        Device device = deviceRepository.findByDeviceSn(deviceSn).orElse(null);
        if (device == null) {
            log.warn("绑定验证失败，设备不存在: {}", deviceSn);
            return -1;
        }

        if (device.getHomeId() != null && device.getHomeId().equals(homeId)) {
            log.info("设备 {} 已经绑定到家庭 {}, 无需重复绑定", deviceSn, homeId);
            return 0;
        }

        // 核心：验证通过，注册异步回调，不再立即操作数据库
        log.info("验证通过！正在等待设备 {} 硬件端联网确认...", deviceSn);

        mqttController.registerBindCallback(deviceSn, (mqttPayload) -> {
            try {
                String hardwareHomeId = JsonUtil.getValue(mqttPayload, "homeId");

                if (homeId.equals(hardwareHomeId)) {
                    device.setHomeId(homeId);
                    deviceRepository.save(device);
                    log.info("🎉 绑定成功！设备 {} 已安全连接并分配至家庭 {}", deviceSn, homeId);
                } else {
                    mqttController.sendBindStatus(false, deviceSn);
                    log.error("⚠️ 绑定防伪校验失败！期望家庭ID: {}, 硬件实际上报家庭ID: {}", homeId, hardwareHomeId);
                }
            } catch (Exception e) {
                log.error("执行设备 {} 绑定回调时发生数据解析异常", deviceSn, e);
            }
        });

        return 1; // 立即返回验证结构
    }
}