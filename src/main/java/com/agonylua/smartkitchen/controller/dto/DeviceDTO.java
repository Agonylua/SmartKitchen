package com.agonylua.smartkitchen.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DeviceDTO {
    private Long deviceId;
    private String deviceSn;
    private String deviceName;
    private String deviceType;
    private String roomName; // 直接把 RoomID 转换成名字返回给前端

    // 重点：前端拿到的将是 {"temp": 26, "switch": "on"}
    // 而不是 "{\"temp\": 26...}"
    private Map<String, Object> status;

    private Boolean isOnline; // 根据心跳判断
}