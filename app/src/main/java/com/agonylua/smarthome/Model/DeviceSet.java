package com.agonylua.smarthome.model;

public class DeviceSet {
    // 冰箱的数据
    private float set_fridgeTemp;
    private float set_freezerTemp;
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

    public float getSetFridgeTemp() {
        return set_fridgeTemp;
    }

    public float getSetFreezerTemp() {
        return set_freezerTemp;
    }

    public float getSetMicrowaveTemp() {
        return set_microwaveTemp;
    }

    public int getSetMicrowaveTime() {
        return set_microwaveTime;
    }

    public boolean isSetDishwasherFK() {
        return set_dishwasherFK;
    }

    public String getSetRiceCookerFlavor() {
        return set_riceCookerFlavor;
    }

    public boolean isSetRiceCookerInsulation() {
        return set_riceCookerInsulation;
    }

    public int getSetRiceCookerTime() {
        return set_riceCookerTime;
    }

    public float getSetSterilizerTemp() {
        return set_sterilizerTemp;
    }

    public int getSetSterilizerTime() {
        return set_sterilizerTime;
    }

    public void setSet_fridgeTemp(int set_fridgeTemp) {
        this.set_fridgeTemp = set_fridgeTemp;
    }

    public void setSet_freezerTemp(int set_freezerTemp) {
        this.set_freezerTemp = set_freezerTemp;
    }

    public void setSet_microwaveTemp(int set_microwaveTemp) {
        this.set_microwaveTemp = set_microwaveTemp;
    }

    public void setSet_microwaveTime(int set_microwaveTime) {
        this.set_microwaveTime = set_microwaveTime;
    }

    public void setSet_dishwasherFK(boolean set_dishwasherFK) {
        this.set_dishwasherFK = set_dishwasherFK;
    }

    public void setSet_riceCookerFlavor(String set_riceCookerFlavor) {
        this.set_riceCookerFlavor = set_riceCookerFlavor;
    }

    public void setSet_riceCookerInsulation(boolean set_riceCookerInsulation) {
        this.set_riceCookerInsulation = set_riceCookerInsulation;
    }

    public void setSet_riceCookerTime(int set_riceCookerTime) {
        this.set_riceCookerTime = set_riceCookerTime;
    }

    public void setSet_sterilizerTemp(int set_sterilizerTemp) {
        this.set_sterilizerTemp = set_sterilizerTemp;
    }

    public void setSet_sterilizerTime(int set_sterilizerTime) {
        this.set_sterilizerTime = set_sterilizerTime;
    }
}
