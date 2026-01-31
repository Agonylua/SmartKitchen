package com.agonylua.smarthome.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;

public class MainRepository {
    public LiveData<Device> newDeviceData = new MutableLiveData<>();
    private DeviceDao deviceDao;
    private Device device;

    public MainRepository(Context context) {
        deviceDao = AppDatabase.getInstance(context).deviceDao();
    }

    public LiveData<Device> getNewDeviceData() {
        return newDeviceData;
    }

    public void updateDeviceData(String sn, String newData) {
        deviceDao.updateDeviceData(sn, newData);
    }
}
