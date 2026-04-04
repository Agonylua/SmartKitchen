package com.agonylua.smartKitchen.model;

import android.util.Log;

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
    AUTO("自动"),
    HIGH_TEMP("高温"),
    UVB("UVB"),
    STERILIZER_DRY("烘干"),

    //--- Other ---
    UNKNOWN("未知");

    private static final String TAG = "DeviceMode";
    private final String label;

    DeviceMode(String label) {
        this.label = label;
    }

    public static String toMode(String label) {
        for (DeviceMode mode : DeviceMode.values()) {
            if (mode.label.equals(label)) {
                return mode.name();
            }
        }
        return DeviceMode.UNKNOWN.name();
    }

    public static String toLabel(String mode) {
        Log.d(TAG, "toLabel: mode = " + mode);
        for (DeviceMode deviceMode : DeviceMode.values()) {
            if (deviceMode.name().equals(mode)) {
                return deviceMode.label;
            }
        }
        return DeviceMode.UNKNOWN.label;
    }

    public String getLabel() {
        return label;
    }
}
