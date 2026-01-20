package com.agonylua.smartkitchen.databases.entity;

import lombok.Getter;

@Getter
public enum DeviceStatus {
    IDLE("空闲"),
    RUNNING("运行中"),
    OFFLINE("离线");
    private final String type;

    DeviceStatus(String type) {
        this.type = type;
    }
}
