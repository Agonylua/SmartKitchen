package com.agonylua.smarthome.viewModel;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.repository.AddDeviceRepository;
import com.agonylua.smarthome.utils.EspProvisioningHelper;
import com.agonylua.smarthome.utils.UserManager;
import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.device_scanner.BleScanner;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.ProvisionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class ProvisionViewModel extends AndroidViewModel {
    private static final String TAG = "ProvisionViewModel";
    // LiveData 用于通知 UI 状态变化
    private final MutableLiveData<String> scanStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final MutableLiveData<String> provisionStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> qrCodeParsed = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> bindRequired = new MutableLiveData<>(false); // 用于标记是否需要绑定设备
    private BleScanner bleScanner;
    private EspProvisioningHelper provisioningHelper;
    private AddDeviceRepository repository;
    private UserManager userManager;
    private String deviceSn = null; // 设备序列号
    private String targetDeviceName = null;
    private String targetPop = null;


    public ProvisionViewModel(@NonNull Application application) {
        super(application);
        provisioningHelper = EspProvisioningHelper.getInstance(application);
        EventBus.getDefault().register(this);
        repository = new AddDeviceRepository(application);
        userManager = UserManager.getInstance(application);
    }

    /**
     * 【新增】解析二维码数据
     * QR 格式示例: {"ver":"v1","name":"PROV_123456","pop":"secret123","transport":"ble"}
     */
    public void parseQrCode(String qrData) {
        try {
            JSONObject json = new JSONObject(qrData);

            // 提取设备名称 (用于蓝牙扫描过滤)
            if (json.has("name")) {
                targetDeviceName = json.getString("name");
            }

            // 提取 POP (用于连接鉴权)
            if (json.has("pop")) {
                targetPop = json.getString("pop");
            }
            // 提取设备序列号 (用于设备绑定)
            if (json.has("sn")) {
                deviceSn = json.getString("sn");
            }

            // 检查传输方式 (可选，确保支持 BLE)
            String transport = json.optString("transport", "ble");
            if (!"ble".equalsIgnoreCase(transport) && !"softap".equalsIgnoreCase(transport)) {
                scanStatus.postValue("不支持的传输方式: " + transport);
                qrCodeParsed.setValue(false);
                return;
            }

            // 调用后端接口绑定设备
            repository.bindDevice(deviceSn, userManager.getHomeId(), new AddDeviceRepository.callback() {
                @Override
                public void onSuccess(int code) {
                    if (code == 0) {
                        scanStatus.postValue("设备绑定失败，请勿重复绑定");
                        qrCodeParsed.setValue(false);
                        Log.d(TAG, "onSuccess: " + "设备绑定失败，请勿重复绑定");
                    } else if (code == -1) {
                        scanStatus.postValue("设备绑定失败，设备是不存在");
                        qrCodeParsed.setValue(false);
                        Log.d(TAG, "onSuccess: " + "设备绑定失败，设备是不存在");
                    } else if (code == 1) {
                        scanStatus.postValue("设备绑定成功，正在连接...");
                        Log.d(TAG, "onSuccess: " + "设备绑定成功，正在连接...");
                        qrCodeParsed.setValue(true);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });
//            if (Boolean.FALSE.equals(qrCodeParsed.getValue())){
//                return;
//            }


            Log.d(TAG, "QR Parsed: Name=" + targetDeviceName + ", POP=" + targetPop);
            //qrCodeParsed.setValue(true);

        } catch (Exception e) {
            Log.e(TAG, "QR Parse Error", e);
            scanStatus.postValue("二维码格式错误");
            qrCodeParsed.setValue(false);
        }
    }

    /**
     * 【修改】扫描逻辑：不再需要传入 prefix，而是使用二维码里的名字
     */
    public void startBleScan() {
        if (targetDeviceName == null) {
            scanStatus.postValue("未获取设备信息，请先扫码");
            return;
        }
        scanStatus.postValue("正在搜索: " + targetDeviceName);

        bleScanner = new BleScanner(getApplication(), new BleScanListener() {
            @Override
            public void scanStartFailed() {
                scanStatus.postValue("Scan Failed");
            }

            @Override
            public void onPeripheralFound(BluetoothDevice device, android.bluetooth.le.ScanResult scanResult) {
                if (!hasBluetoothPermission()) return;

                String name = device.getName();
                // 匹配设备名
                if (name != null && name.equals(targetDeviceName)) {
                    Log.d(TAG, "Found target device: " + name);
                    stopScan();
                    scanStatus.postValue("已找到设备，正在连接...");

                    // 获取 Service UUID
                    String serviceUuid = null;
                    if (scanResult.getScanRecord() != null && scanResult.getScanRecord().getServiceUuids() != null && !scanResult.getScanRecord().getServiceUuids().isEmpty()) {
                        serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
                    }
                    if (serviceUuid == null) {
                        // 这是一个常见的默认值，你可以先试这个，或者留 null 看是否会报错
                        serviceUuid = "0000ffff-0000-1000-8000-00805f9b34fb";
                    }

                    // 【关键修复】传入 device 对象
                    provisioningHelper.initBLEDevice(device, targetPop, serviceUuid);
                    provisioningHelper.connectDevice();
                }
            }

            @Override
            public void scanCompleted() {

            }

            @Override
            public void onFailure(Exception e) {

            }

        });
        bleScanner.startScan();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        EventBus.getDefault().unregister(this);
        stopScan();
        provisioningHelper.disconnectDevice();
    }

    /**
     * 【新增辅助方法】检查蓝牙权限
     */
    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 检查 BLUETOOTH_CONNECT
            return ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 11及以下 通常检查位置权限 (因为蓝牙扫描需要位置权限)
            return ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void stopScan() {
        if (bleScanner != null) {
            bleScanner.stopScan();
        }
    }

    // 2. 监听连接事件 (Espressif SDK 使用 EventBus 发送连接状态)
    // 参考 AddDeviceActivity.kt 中的 onEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {
        switch (event.getEventType()) {
            case ESPConstants.EVENT_DEVICE_CONNECTED:
                Log.d(TAG, "Device Connected");
                isConnected.setValue(true);
                break;
            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                Log.d(TAG, "Device Disconnected");
                isConnected.setValue(false);
                break;
            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                Log.d(TAG, "Connection Failed");
                isConnected.setValue(false);
                break;
        }
    }

    // 3. 开始配网 (发送 WiFi 账号密码)
    public void startProvisioning(String ssid, String password) {
        provisionStatus.setValue("Provisioning...");
        provisioningHelper.startProvisioning(ssid, password, new ProvisionListener() {
            @Override
            public void createSessionFailed(Exception e) {
                provisionStatus.postValue("Session Failed: " + e.getMessage());
            }

            @Override
            public void wifiConfigSent() {
                provisionStatus.postValue("WiFi Config Sent");
            }

            @Override
            public void wifiConfigFailed(Exception e) {

            }

            @Override
            public void wifiConfigApplied() {
                provisionStatus.postValue("WiFi Applied");
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {

            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {

            }

            @Override
            public void deviceProvisioningSuccess() {
                provisionStatus.postValue("Success");
                new HomeViewModel(getApplication()).syncServiceData(UserManager.getInstance(getApplication()).getHomeId());
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                provisionStatus.postValue("Failed: " + e.getMessage());
            }
        });
    }

    // Getters for UI
    public LiveData<String> getScanStatus() {
        return scanStatus;
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public LiveData<String> getProvisionStatus() {
        return provisionStatus;
    }

    public LiveData<Boolean> getQrCodeParsed() {
        return qrCodeParsed;
    }

    /**
     * 重置所有状态，防止 Fragment 重新进入时收到旧的 LiveData 事件
     */
    public void reset() {
        scanStatus.setValue(null);
        isConnected.setValue(false);
        provisionStatus.setValue(null);
        qrCodeParsed.setValue(false);
        bindRequired.setValue(false);
        targetDeviceName = null;
        targetPop = null;
        deviceSn = null;
        stopScan(); // 停止可能的扫描
        if (provisioningHelper != null) {
            provisioningHelper.disconnectDevice();
        }
    }
}