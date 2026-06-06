package com.agonylua.smartKitchen.model;

public class ScanResultData {
    private String name; // 蓝牙广播名，如 "PROV_SmartKitchen"
    private String pop;  // 验证密钥，如 "123456"

    // Getter & Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPop() {
        return pop;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }
}