package com.agonylua.smartkitchen.common;

import lombok.Data;

@Data
public class DeviceBindReq {
    private String deviceSn;
    private String homeId;
    private String userId;
}
