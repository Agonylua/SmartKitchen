package com.agonylua.smartKitchen.dto;

public class RuleCondition {
    private String type;       // 触发类型: "TIME" (时间), "SENSOR" (传感器), "DEVICE_STATE" (设备状态)
    private String deviceSn;   // 设备序列号 (仅当 type 是 "DEVICE_STATE" 时使用)
    private String property;   // 属性名: 如 "temperature", "humidity", "status"
    private String operator;   // 操作符: ">", "<", "==", ">="
    private String value;      // 比较阈值: 如 "30" (度), "FINISHED" (状态)

    public RuleCondition(String type, String property, String operator, String value) {
        this.type = type;
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public RuleCondition(String type, String deviceSn, String property, String operator, String value) {
        this.type = type;
        this.deviceSn = deviceSn;
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProperty() {
        return property;
    }


    public String getOperator() {
        return operator;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }
}