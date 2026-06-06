package com.agonylua.smartkitchen.common;

import lombok.Data;

@Data
public class DeviceAddReq {
    private String homeId;
    private String deviceName;
    // 前端传字符串 "FRIDGE"，后端转枚举
    private String deviceType;
}
