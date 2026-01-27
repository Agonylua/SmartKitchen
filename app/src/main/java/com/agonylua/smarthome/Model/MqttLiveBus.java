package com.agonylua.smarthome.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class MqttLiveBus {
    private static MqttLiveBus instance;
    private final MutableLiveData<MqttEvent> mqttEvent = new MutableLiveData<>();

    public static MqttLiveBus getInstance() {
        if (instance == null) instance = new MqttLiveBus();
        return instance;
    }

    public void post(String topic, String message) {
        mqttEvent.postValue(new MqttEvent(topic, message));
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