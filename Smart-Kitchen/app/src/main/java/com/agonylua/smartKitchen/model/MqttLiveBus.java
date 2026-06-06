package com.agonylua.smartKitchen.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smartKitchen.database.AppDatabase;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.utils.JsonUtils;

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
            if (!topic.startsWith("smartKitchen/")) return;
            String[] topicParts = topic.substring("smartKitchen/".length()).split("/");
            if (topicParts.length < 2) return;

            if (topicParts[0].equals("environment")) {
                Map<String, Object> payload = JsonUtils.toMap(message);
                mqttEvent.postValue(new MqttEvent(topic, payload));
                return;
            }

            if (topicParts.length != 3) return;
            String sn = topicParts[1];
            if (topicParts[0].equals("application") && topicParts[2].equals("update")) {
                Map<String, Object> payload = JsonUtils.toMap(message);
                deviceDao.updateDeviceMode(sn, Objects.requireNonNull(payload.get("mode")).toString());
                deviceDao.updateDeviceData(sn, Objects.requireNonNull(payload.get("data")).toString());
                String runTimeObj = payload.get("runTime").toString();
                int runTime = 0;
                try {
                    runTime = (int) Double.parseDouble(runTimeObj);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
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
        public Map<String, Object> message;

        public MqttEvent(String t, Map<String, Object> m) {
            topic = t;
            message = m;
        }
    }
}