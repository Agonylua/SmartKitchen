package com.agonylua.smarthome.model;

import android.content.Context;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;

public class Device {
    private DeviceDao deviceDao;
    private String TAG = "DeviceModel";
    private String DeviceSn;
    private String DeviceName;
    private String DeviceType;
    private Boolean DeviceStatus;
    private Integer DeviceCount;

    public Device(Context context, String homeId) {
        deviceDao = AppDatabase.getInstance(context).deviceDao();
    }
}
