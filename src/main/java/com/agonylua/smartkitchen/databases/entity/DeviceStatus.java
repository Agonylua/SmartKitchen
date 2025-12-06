package com.agonylua.smartkitchen.databases.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeviceStatus {
    ONLINE("online"),
    OFFLINE("offline"),
    ERROR("error");

    private final String value;

    DeviceStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DeviceStatus fromValue(String value) {
        for (DeviceStatus status : DeviceStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的设备状态: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}