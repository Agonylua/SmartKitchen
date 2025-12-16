package com.agonylua.smartkitchen.databases.entity;


import lombok.Getter;

@Getter
public enum DeviceType {
    INDUCTION_COOKER("电磁炉"),
    FRIDGE("冰箱"),
    MICROWAVE("微波炉"),
    RICE_COOKER("电饭煲"),
    DISHWASHER("洗碗机"),
    STERILIZER("消毒柜"),
    SOCKET("插座");

    private final String type;

    DeviceType(String type) {
        this.type = type;
    }
}