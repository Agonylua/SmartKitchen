package com.agonylua.smartKitchen.network;

import android.util.Log;

import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.model.MqttLiveBus;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * MQTT 连接管理类
 */
@Singleton
public class MqttManager {

    private static final String TAG = "MqttManager";
    // ================= 配置参数 =================
    private static final String BROKER_URL = "tcp://agonylua.asia:1883";
    private static final String CLIENT_ID = "Android_App";
    private static final String USERNAME = "smartKitchen";
    private static final String PASSWORD = "wei.liu-liu";
    public static final String SUB_TOPIC = "smartKitchen/application/";
    private static final String PUB_TOPIC = "smartKitchen/devices/";
    // 消息质量 (0:最多一次, 1:至少一次, 2:只有一次)
    private static final int QoS = 1;
    private MqttClient mqttClient;
    private MqttConnectOptions options;
    private OnMessageListener onMessageListener;
    private final UserManager userManager;
    private final DeviceDao deviceDao;

    // 注入构造
    @Inject
    public MqttManager(UserManager userManager, DeviceDao deviceDao) {
        this.userManager = userManager;
        this.deviceDao = deviceDao;
        init();
    }

    private void init() {
        try {
            // MemoryPersistence: 数据保存在内存中，重启 App 会丢失未发送的消息
            mqttClient = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());

            // 配置连接选项
            options = new MqttConnectOptions();
            options.setCleanSession(false); // false: 保留会话 (离线也能收到之前的消息)
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setMaxReconnectDelay(10000); // 最大重连间隔 (毫秒)
            options.setConnectionTimeout(10); // 连接超时 (秒)
            options.setKeepAliveInterval(20); // 心跳间隔 (秒) - 越短越灵敏但耗电
            options.setAutomaticReconnect(true); // 开启自动重连

            // 设置回调监听
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "连接断开: " + cause.getMessage());
                    // 可以在这里通知 UI 显示"设备离线"
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    MqttLiveBus.getInstance().post(topic, payload);
                    if (onMessageListener != null) {
                        // 注意：messageArrived 通常在子线程运行，如果需要更新 UI，
                        // 实现者需要在 onMessage 中切换到主线程，或者在此处使用 Handler 包装
                        onMessageListener.onMessage(topic, payload);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "消息发送成功");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接服务器 (必须在子线程调用)
     */
    public void connect() {
        ThreadPoolUtils.getInstance().execute(() -> {
            try {
                String homeId = userManager.getHomeId();
                List<Device> deviceList = deviceDao.getDevicesByHomeId(homeId);
                if (mqttClient != null && !mqttClient.isConnected()) {
                    mqttClient.connect(options);
                    Log.i(TAG, "连接 MQTT 成功");

                    // 连接成功后，立即订阅相关主题
                    if (deviceList != null) {
                        for (Device device : deviceList) {
                            subscribe(SUB_TOPIC + device.getDeviceSn() + "/#");
                            subscribe("smartKitchen/environment/" + userManager.getHomeId());
                            subscribe("smartKitchen/device/" + device.getDeviceSn() + "/#");
                        }
                    } else {
                        Log.d(TAG, "connect: " + "设备列表为空，无法订阅主题");
                    }
                }
            } catch (MqttException e) {
                Log.e(TAG, "连接失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布消息 (控制设备)
     *
     * @param msg 消息内容 (例如: {"power": "on"})
     */
    public void publish(String deviceSn, String msg) {
        ThreadPoolUtils.getInstance().execute(() -> {
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    MqttMessage message = new MqttMessage(msg.getBytes());
                    message.setQos(QoS);
                    String topic = PUB_TOPIC + deviceSn + "/control";
                    mqttClient.publish(topic, message);

                    Log.d(TAG, "发送指令: " + msg);
                } else {
                    Log.e(TAG, "未连接，无法发送");
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 订阅主题
     */
    public void subscribe(String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic, QoS);
                Log.d(TAG, "订阅成功: " + topic);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setOnMessageListener(OnMessageListener listener) {
        this.onMessageListener = listener;
    }

    // ================= 简单的消息回调接口 =================
    public interface OnMessageListener {
        void onMessage(String topic, String message);
    }
}