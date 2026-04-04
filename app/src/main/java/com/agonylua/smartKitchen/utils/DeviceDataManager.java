package com.agonylua.smartKitchen.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.agonylua.smartKitchen.model.DeviceMode;

public class DeviceDataManager {
    private static final String TAG = "DeviceDataManager";
    private static DeviceDataManager Instance;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    private final String FRIDGE_MODE = "fridge_mode";
    private final String MICROWAVE_MODE = "microwave_mode";
    private final String DISHWASHER_MODE = "dishwasher_mode";
    private final String RICE_COOKER_MODE = "rice_cooker_mode";
    private final String STERILIZER_MODE = "sterilizer_mode";
    private final String FRIDGE_TEMP = "fridge_temp";
    private final String FREEZE_TEMP = "freeze_temp";
    private final String MICROWAVE_TIME = "microwave_time";
    private final String MICROWAVE_TEMP = "microwave_temp";
    private final String RICE_COOKER_TEXTURE = "rice_cooker_texture";
    private final String RICE_COOKER_INSULATION = "rice_cooker_insulation";
    private final String DISHWASHER_KEEPFRESH = "dishwasher_keepFresh";
    private final String STERILIZER_UVLIGHT = "sterilizer_uvLight";
    private final String STERILIZER_TIME_DISPLAY = "sterilizer_time_display";
    // TODO: 添加更多设备数据键值对

    public DeviceDataManager(Application application, String deviceSn) {
        sp = application.getSharedPreferences(deviceSn, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static DeviceDataManager Instance(Application application, String deviceSn) {
        synchronized (DeviceDataManager.class) {
            if (Instance == null) {
                Instance = new DeviceDataManager(application, deviceSn);
            }
        }
        return Instance;
    }

    //------------------------------- Device Methods -------------------------------//

    //------------------------------- Fridge Methods -------------------------------//
    public String getFridgeMode() {
        return sp.getString(FRIDGE_MODE, DeviceMode.STANDARD.name());
    }

    public void setFridgeMode(String mode) {
        editor.putString(FRIDGE_MODE, mode);
        editor.apply();
    }

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
    public String getMicrowaveMode() {
        return sp.getString(MICROWAVE_MODE, DeviceMode.HEAT.name());
    }

    public void setMicrowaveMode(String mode) {
        editor.putString(MICROWAVE_MODE, mode);
        editor.apply();
    }

    public void saveMicrowaveSet(float time, float temp) {
        editor.putFloat(MICROWAVE_TIME, time);
        editor.putFloat(MICROWAVE_TEMP, temp);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public Float getMicrowaveTime() {
        return sp.getFloat(MICROWAVE_TIME, 10.0f);
    }

    public Float getMicrowaveTemp() {
        return sp.getFloat(MICROWAVE_TEMP, 30.0f);
    }

    //-------------------------------- Rice Cooker Methods -------------------------------//
    public String getRiceCookerMode() {
        return sp.getString(RICE_COOKER_MODE, DeviceMode.COOK_RICE.name());
    }

    public void setRiceCookerMode(String mode) {
        editor.putString(RICE_COOKER_MODE, mode);
        editor.apply();
    }

    public void saveRiceCookerSet(Integer texture) {
        editor.putInt(RICE_COOKER_TEXTURE, texture);
        editor.apply();
    }

    public void saveRiceCookerInsulation(Boolean insulation) {
        editor.putBoolean(RICE_COOKER_INSULATION, insulation);
        editor.apply();
    }

    public Integer getRiceCookerTexture() {
        return sp.getInt(RICE_COOKER_TEXTURE, 2);
    }

    public Boolean getRiceCookerInsulation() {
        return sp.getBoolean(RICE_COOKER_INSULATION, true);
    }

    //-------------------------------- Dishwasher Methods -------------------------------//
    public String getDishwasherMode() {
        return sp.getString(DISHWASHER_MODE, DeviceMode.STANDARD_WASH.name());
    }

    public void setDishwasherMode(String mode) {
        editor.putString(DISHWASHER_MODE, mode);
        editor.apply();
    }

    public void saveDishwasherKeepFresh(Boolean keepFresh) {
        editor.putBoolean(DISHWASHER_KEEPFRESH, keepFresh);
        editor.apply();
    }

    public Boolean getDishwasherKeepFresh() {
        return sp.getBoolean(DISHWASHER_KEEPFRESH, false);
    }

    //-------------------------------- Sterilizer Methods -------------------------------//
    public String getSterilizerMode() {
        return sp.getString(STERILIZER_MODE, DeviceMode.AUTO.name());
    }

    public void setSterilizerMode(String mode) {
        editor.putString(STERILIZER_MODE, mode);
        editor.apply();
    }

    public void saveSterilizerUVLight(Boolean uvLight) {
        editor.putBoolean(STERILIZER_UVLIGHT, uvLight);
        editor.apply();
    }

    public Boolean getSterilizerUVLight() {
        return sp.getBoolean(STERILIZER_UVLIGHT, false);
    }

    public void saveSterilizerTimeDisplay(Integer timeDisplay) {
        editor.putInt(STERILIZER_TIME_DISPLAY, timeDisplay);
        editor.apply();
    }

    public Integer getSterilizerTimeDisplay() {
        return sp.getInt(STERILIZER_TIME_DISPLAY, 0);
    }
}
