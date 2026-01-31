package com.agonylua.smarthome.model;

import static com.agonylua.smarthome.network.MqttManager.SUB_TOPIC;

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
        Log.d(TAG, "post: " + topic + " , " + message);
        //mqttEvent.postValue(new MqttEvent(topic, message));
        try {
            String[] topicParts = topic.substring(SUB_TOPIC.length()).split("/");
            String sn = topicParts[0];
            Map<String, Object> payload = JsonUtils.toMap(message);
            deviceDao.updateDeviceMode(sn, Objects.requireNonNull(payload.get("mode")).toString());
            deviceDao.updateDeviceData(sn, Objects.requireNonNull(payload.get("data")).toString());
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