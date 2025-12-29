package com.agonylua.smarthome.network;

public class DeviceRequest {
    private String deviceSn;
    private String homeId;
    private String deviceName;
    // 前端传字符串 "FRIDGE"，后端转枚举
    private String deviceType;
}
