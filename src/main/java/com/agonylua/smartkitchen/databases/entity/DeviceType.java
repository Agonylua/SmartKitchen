package com.agonylua.smartkitchen.databases.entity;


import lombok.Getter;

@Getter
public enum DeviceType {
    REFRIGERATOR("冰箱"),
    RICE_COOKER("电饭煲"),
    DISHWASHER("洗碗机"),
    STERILIZER("消毒柜"),
    MICROWAVE("微波炉");
    private final String type;

    DeviceType(String type) {
        this.type = type;
    }
}