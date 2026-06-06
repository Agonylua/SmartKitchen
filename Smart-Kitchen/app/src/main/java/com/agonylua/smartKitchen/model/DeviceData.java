package com.agonylua.smartKitchen.model;

public class DeviceData {
    private int realTimePower;
    // 冰箱的数据
    private float fridgeTemp;
    private float freezerTemp;
    // 微波炉的数据
    private float microwaveTemp; // 加热温度
    private int microwaveTime; // 加热时长
    // 洗碗机的数据
    private int dishwasherSWS; // 软水盐储量
    private int dishwasherRA; // 漂洗剂储量
    private boolean dishwasherFK; // 鲜存保管
    // 电饭煲的数据
    private String riceCookerFlavor; // 口感调节
    private boolean riceCookerInsulation; // 自动保温
    private int riceCookerTime; // 加热时长
    // 消毒柜的数据
    private float sterilizerTemp; // 加热温度
    private int sterilizerTime; // 加热时长

    // Getters and Setters
    public float getFridgeTemp() {
        return fridgeTemp;
    }

    public void setFridgeTemp(int fridgeTemp) {
        this.fridgeTemp = fridgeTemp;
    }

    public float getFreezeTemp() {
        return freezerTemp;
    }

    public float getMicrowaveTemp() {
        return microwaveTemp;
    }

    public void setMicrowaveTemp(int microwaveTemp) {
        this.microwaveTemp = microwaveTemp;
    }

    public int getMicrowaveTime() {
        return microwaveTime;
    }

    public void setMicrowaveTime(int microwaveTime) {
        this.microwaveTime = microwaveTime;
    }

    public int getDishwasherSWS() {
        return dishwasherSWS;
    }

    public void setDishwasherSWS(int dishwasherSWS) {
        this.dishwasherSWS = dishwasherSWS;
    }

    public int getDishwasherRA() {
        return dishwasherRA;
    }

    public void setDishwasherRA(int dishwasherRA) {
        this.dishwasherRA = dishwasherRA;
    }

    public boolean isDishwasherFK() {
        return dishwasherFK;
    }

    public void setDishwasherFK(boolean dishwasherFK) {
        this.dishwasherFK = dishwasherFK;
    }

    public String getRiceCookerFlavor() {
        return riceCookerFlavor;
    }

    public void setRiceCookerFlavor(String riceCookerFlavor) {
        this.riceCookerFlavor = riceCookerFlavor;
    }

    public boolean isRiceCookerInsulation() {
        return riceCookerInsulation;
    }

    public void setRiceCookerInsulation(boolean riceCookerInsulation) {
        this.riceCookerInsulation = riceCookerInsulation;
    }

    public int getRiceCookerTime() {
        return riceCookerTime;
    }

    public void setRiceCookerTime(int riceCookerTime) {
        this.riceCookerTime = riceCookerTime;
    }

    public float getSterilizerTemp() {
        return sterilizerTemp;
    }

    public void setSterilizerTemp(int sterilizerTemp) {
        this.sterilizerTemp = sterilizerTemp;
    }

    public int getSterilizerTime() {
        return sterilizerTime;
    }

    public void setSterilizerTime(int sterilizerTime) {
        this.sterilizerTime = sterilizerTime;
    }

    public void setFreezerTemp(int freezerTemp) {
        this.freezerTemp = freezerTemp;
    }

    public int getRealTimePower() {
        return realTimePower;
    }

    public void setRealTimePower(int realTimePower) {
        this.realTimePower = realTimePower;
    }
}