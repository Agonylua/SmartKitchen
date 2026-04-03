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

    private final MutableLiveData<String> scanStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final MutableLiveData<String> provisionStatus = new MutableLiveData<>();

    // 控制是否允许进入蓝牙/WiFi配网阶段
    private final MutableLiveData<Boolean> qrCodeParsed = new MutableLiveData<>(false);

    // UI 错误状态控制：为 true 时隐藏配网UI，显示错误和重新扫码按钮
    private final MutableLiveData<Boolean> isScanError = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    // 弹窗提示信息
    private final MutableLiveData<String> bindResultMsg = new MutableLiveData<>();

    private BleScanner bleScanner;
    private EspProvisioningHelper provisioningHelper;
    private AddDeviceRepository repository;
    private UserManager userManager;
    private String deviceSn = null;
    private String targetDeviceName = null;
    private String targetPop = null;

    public ProvisionViewModel(@NonNull Application application) {
        super(application);
        provisioningHelper = EspProvisioningHelper.getInstance(application);
        EventBus.getDefault().register(this);
        repository = AddDeviceRepository.getInstance(application);
        userManager = UserManager.getInstance(application);
    }

    /**
     * 解析二维码数据并调用后端绑定接口 (强校验模式)
     */
    public void parseQrCode(String qrData) {
        scanStatus.postValue("正在解析设备信息...");
        try {
            JSONObject json = new JSONObject(qrData);

            if (json.has("name")) targetDeviceName = json.getString("name");
            if (json.has("pop")) targetPop = json.getString("pop");
            if (json.has("sn")) deviceSn = json.getString("sn");

            String transport = json.optString("transport", "ble");
            if (!"ble".equalsIgnoreCase(transport) && !"softap".equalsIgnoreCase(transport)) {
                setErrorState(true, "不支持的传输方式: " + transport);
                return;
            }

            Log.d(TAG, "QR Parsed: Name=" + targetDeviceName + ", POP=" + targetPop);
            scanStatus.postValue("正在向服务器验证设备...");
            isScanError.postValue(false); // 初始重置为非错误状态

            // 调用后端接口绑定设备
            repository.bindDevice(deviceSn, userManager.getHomeId(), new AddDeviceRepository.callback() {
                @Override
                public void onSuccess(int code) {
                    if (code == 1) {
                        bindResultMsg.postValue("设备验证成功！准备配置网络。");
                        scanStatus.postValue("设备验证成功，正在搜索蓝牙...");
                        qrCodeParsed.postValue(true); // 放行：允许蓝牙扫描
                    } else if (code == 0) {
                        setErrorState(true, "设备已被绑定！将继续配置本地网络。");
                        qrCodeParsed.postValue(false);
                    } else {
                        // code == -1 或其他未知状态，拦截并报错
                        setErrorState(true, "设备验证失败：该设备在云端不存在或无效。");
                        qrCodeParsed.postValue(false); // 拦截：不进行蓝牙扫描
                    }
                }

                @Override
                public void onFailure(String errorMsg) {
                    // 网络请求失败，拦截并报错
                    setErrorState(true, "服务器连接失败：" + errorMsg + "\n请检查网络后重新扫码。");
                    qrCodeParsed.postValue(false); // 拦截
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "QR Parse Error", e);
            // JSON 格式错误等，拦截并报错
            setErrorState(true, "二维码格式错误，无法识别设备信息。请确保扫描的是本系统支持的智能厨房设备。");
            qrCodeParsed.postValue(false); // 拦截
        }
    }

    public void startBleScan() {
        if (targetDeviceName == null) {
            setErrorState(true, "未获取到设备蓝牙名称，无法配网。");
            return;
        }
        scanStatus.postValue("正在搜索蓝牙设备: " + targetDeviceName);

        bleScanner = new BleScanner(getApplication(), new BleScanListener() {
            @Override
            public void scanStartFailed() {
                scanStatus.postValue("蓝牙扫描启动失败，请确认蓝牙已开启");
            }

            @Override
            public void onPeripheralFound(BluetoothDevice device, android.bluetooth.le.ScanResult scanResult) {
                if (!hasBluetoothPermission()) return;

                String name = device.getName();
                if (name != null && name.equals(targetDeviceName)) {
                    Log.d(TAG, "Found target device: " + name);
                    stopScan();
                    scanStatus.postValue("已找到设备，正在连接...");

                    String serviceUuid = null;
                    if (scanResult.getScanRecord() != null && scanResult.getScanRecord().getServiceUuids() != null && !scanResult.getScanRecord().getServiceUuids().isEmpty()) {
                        serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
                    }
                    if (serviceUuid == null) {
                        serviceUuid = "0000ffff-0000-1000-8000-00805f9b34fb";
                    }

                    provisioningHelper.initBLEDevice(device, targetPop, serviceUuid);
                    provisioningHelper.connectDevice();
                }
            }

            @Override
            public void scanCompleted() {
                Log.d(TAG, "scanCompleted: " + "扫描完成，未找到设备");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "onFailure: 蓝牙扫描异常: " + e.getMessage());
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

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void stopScan() {
        if (bleScanner != null) {
            bleScanner.stopScan();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {
        switch (event.getEventType()) {
            case ESPConstants.EVENT_DEVICE_CONNECTED:
                scanStatus.setValue("蓝牙连接成功，请输入 Wi-Fi 密码并开始配网");
                isConnected.setValue(true);
                break;
            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                scanStatus.setValue("蓝牙连接断开");
                startBleScan(); // 连接断开后重新扫描
                break;
        }
    }

    public void startProvisioning(String ssid, String password) {
        provisionStatus.setValue("正在发送网络配置...");
        provisioningHelper.startProvisioning(ssid, password, new ProvisionListener() {
            @Override
            public void createSessionFailed(Exception e) {
                provisionStatus.postValue("会话创建失败: " + e.getMessage());
            }

            @Override
            public void wifiConfigSent() {
                provisionStatus.postValue("Wi-Fi 配置已发送...");
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                provisionStatus.postValue("Wi-Fi 配置发送失败");
            }

            @Override
            public void wifiConfigApplied() {
                provisionStatus.postValue("设备正在连接 Wi-Fi...");
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                provisionStatus.postValue("设备连接 Wi-Fi 失败");
            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                provisionStatus.postValue("设备配网失败: " + failureReason.name());
            }

            @Override
            public void deviceProvisioningSuccess() {
                provisionStatus.postValue("Success");
                new HomeViewModel(getApplication()).syncServiceData(UserManager.getInstance(getApplication()).getHomeId());
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                provisionStatus.postValue("配网流程异常: " + e.getMessage());
            }
        });

    }

    public void setErrorState(boolean isError, String msg) {
        isScanError.postValue(isError);
        errorMessage.postValue(msg);
        qrCodeParsed.postValue(false); // 确保发生错误时，强制阻断蓝牙流程
    }

    public void clearBindResultMsg() {
        bindResultMsg.setValue(null);
    }

    public void resetState() {
        scanStatus.setValue(null);
        isConnected.setValue(false);
        provisionStatus.setValue(null);
        qrCodeParsed.setValue(false);
        isScanError.setValue(false);
        errorMessage.setValue("");
        bindResultMsg.setValue(null);
        targetDeviceName = null;
        targetPop = null;
        deviceSn = null;
        stopScan();
        if (provisioningHelper != null) {
            provisioningHelper.disconnectDevice();
        }
    }

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

    public LiveData<Boolean> getIsScanError() {
        return isScanError;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getBindResultMsg() {
        return bindResultMsg;
    }
}