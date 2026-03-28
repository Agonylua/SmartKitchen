package com.agonylua.smarthome.model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.utils.JsonUtils;

import java.util.Map;
import java.util.Objects;


public class MqttLiveBus {
    private static final String TAG = "MqttLiveBus";
    private static MqttLiveBus instance;
    private static DeviceDao deviceDao;
    private final MutableLiveData<MqttEvent> mqttEvent = new MutableLiveData<>();

    public static MqttLiveBus getInstance() {
        synchronized (MqttLiveBus.class) {
            if (instance == null) instance = new MqttLiveBus();
        }
        return instance;
    }

    public void init(Context context) {
        if (deviceDao == null) {
            deviceDao = AppDatabase.getInstance(context).deviceDao();
        }
    }

    public void post(String topic, String message) {
        //mqttEvent.postValue(new MqttEvent(topic, message));
        try {
            String[] topicParts = topic.substring("smartKitchen/".length()).split("/");
            if (topicParts.length != 3) return;
            String sn = topicParts[1];
            if (topicParts[0].equals("application") && topicParts[2].equals("update")) {
                Map<String, Object> payload = JsonUtils.toMap(message);
                Log.d(TAG, "post: 收到设备更新消息, Payload: " + payload);
                deviceDao.updateDeviceMode(sn, Objects.requireNonNull(payload.get("mode")).toString());
                deviceDao.updateDeviceData(sn, Objects.requireNonNull(payload.get("data")).toString());
                Object runTimeObj = payload.get("runTime");
                long runTime = 0;
                if (runTimeObj != null) {
                    try {
                        runTime = (long) Double.parseDouble(runTimeObj.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                deviceDao.updateDeviceRunTime(sn, runTime);
            } else if (topicParts[0].equals("status")) {
                if (message.equals("offline")) {
                    deviceDao.updateDeviceStatus(sn, DeviceStatus.OFFLINE.name());
                } else if (message.equals("online")) {
                    deviceDao.updateDeviceStatus(sn, DeviceStatus.ONLINE.name());
                } else {
                    deviceDao.updateDeviceStatus(sn, DeviceStatus.UNKNOWN.name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<MqttEvent> getEvent() {
        return mqttEvent;
    }

    // 数据包装类
    public static class MqttEvent {
        public String topic;
        public String message;

        public MqttEvent(String t, String m) {
            topic = t;
            message = m;
        }
    }
}