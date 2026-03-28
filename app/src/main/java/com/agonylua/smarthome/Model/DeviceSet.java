package com.agonylua.smarthome.model;

public class DeviceSet {
    // 冰箱的数据
    private float fridgeTempThreshold;
    private float freezeTempThreshold;
    // 微波炉的数据
    private float set_microwaveTemp; // 加热温度
    private int set_microwaveTime; // 加热时长
    // 洗碗机的数据
    private boolean set_dishwasherFK; // 鲜存保管
    // 电饭煲的数据
    private String set_riceCookerFlavor; // 口感调节
    private boolean set_riceCookerInsulation; // 自动保温
    private int set_riceCookerTime; // 加热时长
    // 消毒柜的数据
    private float set_sterilizerTemp; // 加热温度
    private int set_sterilizerTime; // 加热时长

    //--  Getter 和 Setter --//
    public float getFreezeTempThreshold() {
        return freezeTempThreshold;
    }

    public void setFreezeTempThreshold(float freezeTempThreshold) {
        this.freezeTempThreshold = freezeTempThreshold;
    }

    public float getFridgeTempThreshold() {
        return fridgeTempThreshold;
    }

    public void setFridgeTempThreshold(float fridgeTempThreshold) {
        this.fridgeTempThreshold = fridgeTempThreshold;
    }
}
