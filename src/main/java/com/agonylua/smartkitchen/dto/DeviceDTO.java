package com.agonylua.smartkitchen.dto;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.Data;

import java.util.Map;

@Data
public class DeviceDTO {
    private String deviceSn;
    private String deviceName;
    private String deviceType; // 返回中文或英文给前端
    private String homeId;

    // 重点：前端拿到的直接是对象 {"temp": -18, "mode": "eco"}
    private Map<String, Object> deviceData;

    public static DeviceDTO fromEntity(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setDeviceSn(device.getDeviceSn());
        dto.setDeviceName(device.getDeviceName());
        dto.setHomeId(device.getHomeId());

        // 枚举转字符串
        dto.setDeviceType(device.getDeviceType().name());

        // 解析 JSON 字符串为 Map
        if (device.getDeviceData() != null) {
            dto.setDeviceData(JsonUtil.parseMap(device.getDeviceData()));
        }
        return dto;
    }
}