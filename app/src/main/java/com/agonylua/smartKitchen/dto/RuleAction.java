package com.agonylua.smartKitchen.dto;

public class RuleAction {
    private String type;       // 动作类型: "DEVICE_CONTROL" (控制设备), "NOTIFICATION" (发送通知)
    private String deviceSn;   // 目标设备ID
    private String command;    // 执行指令: 如 "START_MODE", "POWER_OFF", "SET_TEMP"
    private String payload;    // 指令参数: 如 "AUTO_STERILIZE", "18"

    public RuleAction(String type, String deviceSn, String command, String payload) {
        this.type = type;
        this.deviceSn = deviceSn;
        this.command = command;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getCommand() {
        return command;
    }


    public String getPayload() {
        return payload;
    }

}