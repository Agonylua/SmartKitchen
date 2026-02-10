package com.agonylua.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceDataManager {
    private static final String TAG = "DeviceDataManager";
    private static DeviceDataManager Instance;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    private final String DEVICE_MODE = "device_mode";
    private final String FRIDGE_TEMP = "fridge_temp";
    private final String FREEZE_TEMP = "freeze_temp";
    private final String MICROWAVE_TIME = "microwave_time";
    private final String MICROWAVE_TEMPERATURE = "microwave_temp";
    // TODO: 添加更多设备数据键值对

    public DeviceDataManager(Context context, String deviceSn) {
        sp = context.getApplicationContext().getSharedPreferences(deviceSn, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static DeviceDataManager Instance(Context context, String deviceSn) {
        ;
        if (Instance == null) {
            Instance = new DeviceDataManager(context, deviceSn);
        }
        return Instance;
    }

    //------------------------------- Device Methods -------------------------------//
    public String getDeviceMode() {
        return sp.getString(DEVICE_MODE, null);
    }

    public void setDeviceMode(String mode) {
        editor.putString(DEVICE_MODE, mode);
        editor.apply();
    }

    //------------------------------- Fridge Methods -------------------------------//
    public void saveFridgeSet(float temperature, float humidity) {
        editor.putFloat(FRIDGE_TEMP, temperature);
        editor.putFloat(FREEZE_TEMP, humidity);
        editor.apply();
    }

    public float getFridgeTemp() {
        return sp.getFloat(FRIDGE_TEMP, 4.f);
    }

    public void setFridgeTemp(String temperature) {
        editor.putString(FRIDGE_TEMP, temperature);
        editor.apply();
    }

    public float getFreezeTemp() {
        return sp.getFloat(FREEZE_TEMP, -18.f);
    }

    public void setFreezeTemp(String humidity) {
        editor.putString(FREEZE_TEMP, humidity);
        editor.apply();
    }

    //-------------------------------- Microwave Methods -------------------------------//
    public void saveMicrowaveSet(String time, String temperature) {
        editor.putString(MICROWAVE_TIME, time);
        editor.putString(MICROWAVE_TEMPERATURE, temperature);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public String getMicrowaveTime() {
        return sp.getString(MICROWAVE_TIME, "-");
    }

    public String getMicrowaveTemp() {
        return sp.getString(MICROWAVE_TEMPERATURE, "-");
    }


}
