package com.agonylua.smarthome.common;

import com.agonylua.smarthome.model.DeviceData;

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
