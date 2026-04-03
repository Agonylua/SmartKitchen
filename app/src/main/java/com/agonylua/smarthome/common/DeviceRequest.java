package com.agonylua.smarthome.common;

public class DeviceRequest {
    private String deviceSn; // 设备 SN 码
    private String homeId; // (可选) 设备名称
    private String userId; // (可选) 设备类型

    public DeviceRequest(String deviceSn, String homeId) {
        this.deviceSn = deviceSn;
        this.homeId = homeId;
        this.userId = userId;
    }

    public DeviceRequest(String deviceSn, String homeId, String userId) {
        this.deviceSn = deviceSn;
        this.homeId = homeId;
        this.userId = userId;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
