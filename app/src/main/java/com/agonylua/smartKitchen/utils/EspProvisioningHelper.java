package com.agonylua.smartKitchen.utils;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.ResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class EspProvisioningHelper {
    private static final String TAG = "EspProvisioningHelper";
    private static EspProvisioningHelper instance;
    private ESPDevice espDevice;
    private Context context;

    private EspProvisioningHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized EspProvisioningHelper getInstance(Context context) {
        if (instance == null) {
            instance = new EspProvisioningHelper(context);
        }
        return instance;
    }

    /**
     * 初始化设备连接
     * 参考官方 createESPDevice 方法
     */
    public void initBLEDevice(BluetoothDevice device, String pop, String serviceUuid) {
        // 创建 BLE 传输、Security 1 (POP校验) 的设备实例
        // 注意：根据你的固件配置，SecurityType 可能是 SECURITY_0 (无POP), SECURITY_1 (POP), 或 SECURITY_2 (SRP6a)
        // 这里假设使用标准的 POP 校验 (Security 1)
        // 1. 创建实例
        espDevice = new ESPDevice(context, ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);

        // 2. 【关键修复】必须设置原生的 BluetoothDevice 对象，否则连接时会报空指针
        espDevice.setBluetoothDevice(device);

        // 3. 设置其他属性
        try {
            espDevice.setDeviceName(device.getName());
        } catch (SecurityException e) {
            // 双重保险：捕获可能抛出的 SecurityException
            Log.e(TAG, "SecurityException during connect: " + e.getMessage());
        }
        espDevice.setProofOfPossession(pop);
        espDevice.setPrimaryServiceUuid(serviceUuid);
    }

    /**
     * 连接设备
     */
    public void connectDevice() {
        if (espDevice == null) {
            Log.e(TAG, "Device not initialized");
            return;
        }

        // 【修复点 2】显式检查权限
        if (!hasBluetoothPermission()) {
            Log.e(TAG, "Cannot connect: Missing Bluetooth permissions");
            return;
        }

        // 只有检查通过才调用 SDK 方法
        try {
            espDevice.connectToDevice();
        } catch (SecurityException e) {
            // 双重保险：捕获可能抛出的 SecurityException
            Log.e(TAG, "SecurityException during connect: " + e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    public void disconnectDevice() {
        if (espDevice != null) {
            espDevice.disconnectDevice();
        }
    }

    /**
     * 发送 homeId 到 ESP32 的自定义端点 (Custom Endpoint)
     *
     * @param homeId   当前要绑定的家庭ID
     * @param listener 成功或失败的回调
     */
    public void sendHomeId(String homeId, ResponseListener listener) {
        if (espDevice == null) {
            Log.e(TAG, "Device not initialized");
            if (listener != null) {
                listener.onFailure(new Exception("Device not initialized"));
            }
            return;
        }

        try {
            // 将 homeId 包装成 JSON 格式发送，方便硬件端扩展解析
            JSONObject json = new JSONObject();
            json.put("homeId", homeId);
            byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);

            espDevice.sendDataToCustomEndPoint("bind-homeId", data, listener);
            Log.d(TAG, "Sending homeId to device: " + json);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON for homeId", e);
            if (listener != null) {
                listener.onFailure(e);
            }
        }
    }

    /**
     * 执行配网：发送 SSID 和 密码
     * 逻辑源自 ProvisionActivity.kt 中的 doProvisioning
     */
    public void startProvisioning(String ssid, String password, ProvisionListener listener) {
        if (espDevice != null) {
            espDevice.provision(ssid, password, listener);
        } else {
            Log.e(TAG, "Device not initialized");
        }
    }

    public ESPDevice getEspDevice() {
        return espDevice;
    }

    /**
     * 解析二维码并初始化设备信息
     *
     * @param qrCodeData 扫描到的原始字符串
     * @return 解析成功返回 true
     */
    public boolean initFromQRCode(String qrCodeData) {
        try {
            Log.d(TAG, "QR Data: " + qrCodeData);
            JSONObject json = new JSONObject(qrCodeData);

            // 1. 获取关键信息
            String name = json.optString("name");
            String pop = json.optString("pop");
            String transport = json.optString("transport", "ble"); // 默认为 ble

            // 2. 校验
            if (name.isEmpty()) {
                Log.e(TAG, "Invalid QR: name is missing");
                return false;
            }

            // 3. 初始化设备实例
            ESPConstants.TransportType transportType = "softap".equalsIgnoreCase(transport) ?
                    ESPConstants.TransportType.TRANSPORT_SOFTAP :
                    ESPConstants.TransportType.TRANSPORT_BLE;

            // Security通常默认为1 (POP校验)
            espDevice = new ESPDevice(context, transportType, ESPConstants.SecurityType.SECURITY_1);
            espDevice.setDeviceName(name);
            espDevice.setProofOfPossession(pop);

            return true;
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parse Error", e);
            return false;
        }
    }

    // 获取当前待连接的目标设备名
    public String getTargetDeviceName() {
        return espDevice != null ? espDevice.getDeviceName() : null;
    }

    /**
     * 内部权限检查工具
     */
    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 12以下连接通常不需要运行时动态检查 CONNECT 权限(在Manifest声明即可)
    }


}
