package com.agonylua.smarthome.database;

import androidx.room.ColumnInfo;

import java.util.Map;

public class DeviceDataTuple {
    @ColumnInfo(name = "deviceData")
    public Map<String, String> deviceData;
}