package com.agonylua.smartkitchen.databases.entity;

import lombok.Getter;

@Getter
public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    UNKNOWN("未知");
    private final String type;

    DeviceStatus(String type) {
        this.type = type;
    }
}
