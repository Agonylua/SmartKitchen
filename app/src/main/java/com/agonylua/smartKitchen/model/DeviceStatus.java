package com.agonylua.smartKitchen.model;

public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    RUNNING("运行中"),
    UNKNOWN("未知");

    private final String state;

    DeviceStatus(String state) {
        this.state = state;
    }

    public static String fromState(String state) {
        for (DeviceStatus status : DeviceStatus.values()) {
            if (status.state.equals(state)) {
                return status.name();
            }
        }
        return DeviceStatus.UNKNOWN.name();
    }

    public String getState() {
        return state;
    }

}
