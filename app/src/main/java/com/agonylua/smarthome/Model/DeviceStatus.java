package com.agonylua.smarthome.model;

public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    UNKNOWN("未知");

    private final String state;

    DeviceStatus(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

}
