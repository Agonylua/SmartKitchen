package com.agonylua.smartkitchen.controller.dto;

import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class DeviceDto {
    private UUID id;
    private String sn;
    private String name;
    private DeviceStatus status;
    private Map<String, Object> date;
    private LocalDateTime time;
}