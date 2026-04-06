package com.agonylua.smartKitchen.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.agonylua.smartKitchen.database.DataConverter;
import com.google.gson.Gson;

import java.util.Map;

@Entity(tableName = "devices")
public class Device implements Parcelable {
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
    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
    @ColumnInfo(name = "deviceStatus")
    private String deviceStatus;
    @ColumnInfo(name = "deviceMode")
    private String deviceMode;
    @TypeConverters(DataConverter.class)
    @ColumnInfo(name = "deviceData")
    private Map<String, String> deviceData;
    @ColumnInfo(name = "runTime")
    private long runTime;

    public Device() {
    }

    @Ignore
    public Device(@NonNull String sn, String name, String type, String deviceStatus, String homeId, Map<String, String> data) {
        this.deviceSn = sn;
        this.deviceName = name;
        this.deviceType = type;
        this.deviceStatus = deviceStatus;
        this.homeId = String.valueOf(homeId);
        this.deviceData = data;

    }

    // Parcelable implementation
    protected Device(Parcel in) {
        deviceSn = in.readString();
        deviceName = in.readString();
        deviceType = in.readString();
        homeId = in.readString();
        deviceStatus = in.readString();
        deviceMode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(deviceSn);
        parcel.writeString(deviceName);
        parcel.writeString(deviceType);
        parcel.writeString(homeId);
        parcel.writeString(deviceStatus);
        parcel.writeString(deviceMode);
        parcel.writeString(deviceData != null ? new Gson().toJson(deviceData) : null);
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

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public Map<String, String> getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(Map<String, String> deviceData) {
        this.deviceData = deviceData;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(String deviceMode) {
        this.deviceMode = deviceMode;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }
}