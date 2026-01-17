package com.agonylua.smarthome.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.agonylua.smarthome.database.DataConverter;

import java.util.Map;

@Entity(tableName = "devices")
public class Device {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    @ColumnInfo(name = "deviceSn")
    private String deviceSn = "";
    @ColumnInfo(name = "deviceName")
    private String deviceName;
    @ColumnInfo(name = "deviceType")
    private String deviceType;
    @ColumnInfo(name = "homeId")
    private String homeId;
    @ColumnInfo(name = "deviceStatus")
    private Boolean deviceStatus;
    @TypeConverters(DataConverter.class)
    @ColumnInfo(name = "deviceData")
    private Map<String, Object> deviceData;

    @Ignore
    public Device(@NonNull String sn, String name, String type, boolean deviceStatus, int homeId, Map<String, Object> data) {
        this.deviceSn = sn;
        this.deviceName = name;
        this.deviceType = type;
        this.deviceStatus = deviceStatus;
        this.homeId = String.valueOf(homeId);
        this.deviceData = data;

    }

    public Device() {
    }

    // --- Getters ---
    @NonNull
    public String getDeviceSn() {
        return deviceSn;
    }

    // --- Setters ---
    public void setDeviceSn(@NonNull String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Map<String, Object> getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(Map<String, Object> deviceData) {
        this.deviceData = deviceData;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public Boolean getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Boolean deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}