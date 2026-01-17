package com.agonylua.smarthome.model;

public class DeviceData {
    // 冰箱的数据
    private double temp;
    private double hum;

    // 预留给电饭煲、燃气灶的字段 (虽然目前 JSON 没返回，但建议预留)
    private String mode; // 模式
    private int remainingTime; // 剩余时间
    private boolean isFireOn; // 火焰状态

    // Getters and Setters
    public double getTemp() {
        return temp;
    }

    public double getHum() {
        return hum;
    }
    // ... 其他 getter/setter
}