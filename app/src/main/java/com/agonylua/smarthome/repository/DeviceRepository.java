package com.agonylua.smarthome.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.database.entity.Device;

public class DeviceRepository {
    private LiveData<Device> deviceLiveData;

    public DeviceRepository(Application application, String deviceName) {
        // deviceLiveData = AppDatabase.getInstance(application).deviceDao().getDeviceByName(deviceName);
    }


    public LiveData<Device> getDeviceData() {
        return deviceLiveData;
    }
}
