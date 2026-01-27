package com.agonylua.smarthome.adapter;

import java.util.ArrayList;

public class ChipDeviceMode {
    //--- Refrigerator Modes ---
    public static final ArrayList<String> REFRIGERATOR_MODES = new ArrayList<>() {{
        add("标准");
        add("速冷");
        add("节能");
        add("假日");
    }};
    //--- Microwave Modes ---
    public static final ArrayList<String> MICROWAVE_MODES = new ArrayList<>() {{
        add("加热");
        add("烧烤");
        add("解冻");
        add("蒸汽");
    }};
    //--- Rice Cooker Modes ---
    public static final ArrayList<String> RICE_COOKER_MODES = new ArrayList<>() {{
        add("煮饭");
        add("蒸煮");
        add("煮粥");
        add("蛋糕");
    }};
    //--- Dishwasher Modes ---
    public static final ArrayList<String> DISHWASHER_MODES = new ArrayList<>() {{
        add("标准洗");
        add("快速洗");
        add("强力洗");
        add("节能洗");
        add("消毒洗");
        add("烘干");
    }};
    //--- Sterilizer Modes ---
    public static final ArrayList<String> STERILIZER_MODES = new ArrayList<>() {{
        add("高温");
        add("UVB");
        add("烘干");
    }};
}
