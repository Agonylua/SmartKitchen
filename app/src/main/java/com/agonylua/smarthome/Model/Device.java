package com.agonylua.smarthome.Model;

public class Device {
    private String deviceSn;
    private String deviceName;
    private String deviceType;
    private String homeId;
    private DeviceData deviceData;

    // Getters
    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public DeviceData getDeviceData() {
        return deviceData;
    }

    public String getDeviceSn() {
        return deviceSn;
    }
}
