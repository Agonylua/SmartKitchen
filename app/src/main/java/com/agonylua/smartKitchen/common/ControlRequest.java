package com.agonylua.smartKitchen.common;

import com.agonylua.smartKitchen.model.DeviceData;

public class ControlRequest {
    private String deviceSn;
    private DeviceData data;


    public ControlRequest() {
    }

    public ControlRequest RefrigeratorControl(String deviceSn) {
        this.deviceSn = deviceSn;
        return this;
    }
}
