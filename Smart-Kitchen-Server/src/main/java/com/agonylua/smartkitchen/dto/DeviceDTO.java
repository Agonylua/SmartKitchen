package com.agonylua.smartkitchen.dto;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.Data;

import java.util.Map;

@Data
public class DeviceDTO {
    private String deviceSn;
    private String deviceName;
    private String deviceType;
    private String deviceStatus;
    private String deviceMode;
    private String homeId;
    private String runTime;

    // 前端拿到的直接是对象 {"temp": -18, "mode": "eco"}
    private Map<String, Object> deviceData;

    public static DeviceDTO fromEntity(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setDeviceSn(device.getDeviceSn());
        dto.setDeviceName(device.getDeviceName());
        dto.setHomeId(device.getHomeId());
        dto.setDeviceStatus(device.getDeviceStatus().name());
        dto.setRunTime(device.getRunTime());
        // 枚举转字符串
        dto.setDeviceType(device.getDeviceType().name());
        dto.setDeviceMode(device.getDeviceMode());

        // 解析 JSON 字符串为 Map
        if (device.getDeviceData() != null) {
            dto.setDeviceData(JsonUtil.parseMap(device.getDeviceData()));
        }
        return dto;
    }
}