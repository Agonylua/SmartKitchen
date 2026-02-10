package com.agonylua.smarthome.network;

public class DeviceBindRequest {
    private String deviceSn; // 设备 SN 码
    private String homeId; // (可选) 设备名称

    public DeviceBindRequest(String deviceSn, String homeId) {
        this.deviceSn = deviceSn;
        this.homeId = homeId;
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
    // Getter & Setter
}
