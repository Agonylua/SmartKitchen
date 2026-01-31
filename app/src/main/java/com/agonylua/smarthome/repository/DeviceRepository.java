package com.agonylua.smarthome.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.utils.JsonUtils;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import java.util.Map;

public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private DeviceDao deviceDao;
    private LiveData<Device> device;

    public DeviceRepository(Application application, String deviceSn) {
        Log.d(TAG, "DeviceRepository: " + deviceSn);
        deviceDao = AppDatabase.getInstance(application).deviceDao();
        device = deviceDao.getDeviceDataBySn(deviceSn);
    }


    public LiveData<Device> getDevice() {
        return device;
    }

    public void updateDevice(Device device) {
        ThreadPoolUtils.getInstance().execute(() -> {
            deviceDao.update(device);
        });

    }

    public void mqttControlMessage(Map<String, String> payload, String deviceSn) {
        MqttManager.getInstance().publish(deviceSn, JsonUtils.toJson(payload));
    }
}
