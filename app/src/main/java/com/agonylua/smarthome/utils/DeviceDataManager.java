package com.agonylua.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceDataManager {
    private static DeviceDataManager Instance;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    private final String DEVICE_MODE = "device_mode";
    private final String FRIDGE_TEMP = "fridge_temp";
    private final String FRIDGE_HUMIDITY = "fridge_hum";
    private final String MICROWAVE_TIME = "microwave_time";
    private final String MICROWAVE_TEMPERATURE = "microwave_temp";
    // TODO: 添加更多设备数据键值对

    public DeviceDataManager(Context context, String deviceName) {
        sp = context.getApplicationContext().getSharedPreferences(deviceName, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static DeviceDataManager Instance(Context context, String deviceName) {
        ;
        if (Instance == null) {
            Instance = new DeviceDataManager(context, deviceName);
        }
        return Instance;
    }

    //------------------------------- Device Methods -------------------------------//
    public String getDeviceMode() {
        return sp.getString(DEVICE_MODE, "");
    }

    public void setDeviceMode(String mode) {
        editor.putString(DEVICE_MODE, mode);
        editor.apply();
    }

    //------------------------------- Fridge Methods -------------------------------//
    public void saveFridgeSet(Float temperature, Float humidity) {
        editor.putFloat(FRIDGE_TEMP, temperature);
        editor.putFloat(FRIDGE_HUMIDITY, humidity);
        editor.apply();
    }

    public Float getFridgeTemp() {
        return sp.getFloat(FRIDGE_TEMP, 0.f);
    }

    public void setFridgeTemp(String temperature) {
        editor.putString(FRIDGE_TEMP, temperature);
        editor.apply();
    }

    public Float getFridgeHum() {
        return sp.getFloat(FRIDGE_HUMIDITY, 40.f);
    }

    public void setFridgeHum(String humidity) {
        editor.putString(FRIDGE_HUMIDITY, humidity);
        editor.apply();
    }

    //-------------------------------- Microwave Methods -------------------------------//
    public void saveMicrowaveSet(String time, String temperature) {
        editor.putString(MICROWAVE_TIME, time);
        editor.putString(MICROWAVE_TEMPERATURE, temperature);
        editor.apply();
    }

    public String getMicrowaveTime() {
        return sp.getString(MICROWAVE_TIME, "-");
    }

    public String getMicrowaveTemp() {
        return sp.getString(MICROWAVE_TEMPERATURE, "-");
    }


}
