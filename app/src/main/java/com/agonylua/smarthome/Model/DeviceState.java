package com.agonylua.smarthome.model;

public enum DeviceState {
    IDLE("空闲"),
    RUNNING("运行中"),
    PAUSED("已暂停"),
    OFFLINE("离线"),
    UNKNOWN("未知");

    private final String state;

    DeviceState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

}
