package com.agonylua.smartkitchen.databases.entity;

import lombok.Getter;

@Getter
public enum DeviceMode {
    IDLE("待机"),
    //--- Refrigerator Modes ---
    STANDARD("标准"),
    FAST_COOL("速冷"),
    ENERGY_SAVING("节能"),
    HOLIDAY("假日"),

    //--- Microwave Modes ---
    HEAT("加热"),
    GRILL("烧烤"),
    DEFROST("解冻"),
    STEAM("蒸汽"),

    //--- Rice Cooker Modes ---
    COOK_RICE("煮饭"),
    STEAM_COOK("蒸煮"),
    PORRIDGE("煮粥"),
    CAKE("蛋糕"),

    //--- Dishwasher Modes ---
    STANDARD_WASH("标准洗"),
    QUICK_WASH("快速洗"),
    INTENSIVE_WASH("强力洗"),
    ECO_WASH("节能洗"),
    SANITIZE_WASH("消毒洗"),
    DRY("烘干"),

    //--- Sterilizer Modes ---
    HIGH_TEMP("高温"),
    UVB("UVB"),
    STERILIZER_DRY("烘干");

    private final String label;

    DeviceMode(String label) {
        this.label = label;
    }
}