package com.agonylua.smartkitchen.controller.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceDto {
    private Long id;
    private String number;
    private String name;
    private String status;
    private String isOnline;
    private LocalDateTime last_heartbeat;
}