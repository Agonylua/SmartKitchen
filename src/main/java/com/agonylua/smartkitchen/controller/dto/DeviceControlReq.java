package com.agonylua.smartkitchen.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DeviceControlReq {
    private Long deviceId;

    // 例如: {"switch": "on", "brightness": 80}
    private Map<String, Object> payload;
}