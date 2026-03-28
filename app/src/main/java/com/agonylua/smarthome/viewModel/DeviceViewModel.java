package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.adapter.ChipDeviceMode;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.model.DeviceMode;
import com.agonylua.smarthome.model.DeviceSet;
import com.agonylua.smarthome.model.DeviceType;
import com.agonylua.smarthome.repository.DeviceRepository;
import com.agonylua.smarthome.utils.DeviceDataManager;
import com.agonylua.smarthome.utils.JsonUtils;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeviceViewModel extends AndroidViewModel {
    private static final String TAG = "DeviceViewModel";
    //----------------- LiveData 定义 -----------------
    // 设备公共部分
    public final MutableLiveData<String> status = new MutableLiveData<>();// 设备状态数据
    public final MutableLiveData<Integer> statusColor = new MutableLiveData<>();// 设备状态颜色数据
    public final MutableLiveData<Drawable> deviceImage = new MutableLiveData<>();// 设备图片数据
    public final MutableLiveData<String> deviceName = new MutableLiveData<>();// 设备名称数据
    public final MutableLiveData<String> deviceMode = new MutableLiveData<>();// 设备模式数据
    public final MutableLiveData<List<String>> modeTags = new MutableLiveData<>(); // 模式标签数据
    public final MutableLiveData<String> selectedMode = new MutableLiveData<>(); // 选中模式标签数据
    public final MutableLiveData<Integer> statusLoading = new MutableLiveData<>(View.GONE);// 加载状态数据
    public final MutableLiveData<Boolean> subTaskLoading = new MutableLiveData<>(false);// 加载状态数据
    // 冰箱
    public final MutableLiveData<Float> fridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> freezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    public final MutableLiveData<Float> setFridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> setFreezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    // 微波炉
    private final MutableLiveData<LocalTime> microwave_time = new MutableLiveData<>();// 微波炉时间数据
    private final MutableLiveData<String> microwave_temp = new MutableLiveData<>();// 微波炉温度数据
    private final MutableLiveData<LocalTime> limit_microwave_time = new MutableLiveData<>();// 微波炉时间数据
    private final MutableLiveData<String> limit_microwave_temp = new MutableLiveData<>();// 微波炉温度数据
    // 洗碗机
    private final MutableLiveData<String> dishwasher_sws = new MutableLiveData<>();// 软水盐储量数据
    private final MutableLiveData<String> dishwasher_rinse_aid = new MutableLiveData<>();// 漂洗剂储量数据
    private final MutableLiveData<String> dishwasher_fresh_keep = new MutableLiveData<>();// 鲜存保管数据
    // 电饭煲
    private final MutableLiveData<String> rice_cooker_texture_adjust = new MutableLiveData<>();// 口感调节数据
    private final MutableLiveData<String> rice_cooker_keep_warm = new MutableLiveData<>();// 自动保温数据
    private final MutableLiveData<LocalTime> rice_cooker_time = new MutableLiveData<>();// 电饭煲时间数据
    private final MutableLiveData<LocalTime> limit_rice_cooker_time = new MutableLiveData<>();// 电饭煲时间数据
    // 消毒柜
    private final MutableLiveData<String> sterilizer_temp = new MutableLiveData<>();// 消毒柜温度数据
    private final MutableLiveData<LocalTime> sterilizer_time = new MutableLiveData<>();// 消毒柜时间数据
    private final MutableLiveData<String> limit_sterilizer_temp = new MutableLiveData<>();// 消毒柜温度数据
    private final MutableLiveData<LocalTime> limit_sterilizer_time = new MutableLiveData<>();// 消毒柜时间数据
    private final MutableLiveData<Boolean> sterilizer_light = new MutableLiveData<>();// 消毒柜照明数据
    // 聚合观察者
    public MediatorLiveData<Boolean> autoSaverData = new MediatorLiveData<>();
    public MediatorLiveData<Boolean> autoSaverSetup = new MediatorLiveData<>();
    private Device device;
    private DeviceSet deviceSet;
    private DeviceDataManager deviceDataManager;
    private DeviceRepository deviceRepository;
    public DeviceViewModel(@NonNull Application application) {
        super(application);
//        autoSaverData.addSource(fridgeTemp, value -> autoSaverData.setValue(true));
//        autoSaverData.addSource(freezeTemp, value -> autoSaverData.setValue(true));
        autoSaverSetup.addSource(selectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setFridgeTemp, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setFreezeTemp, value -> autoSaverSetup.setValue(true));
    }

    // ================== 冰箱业务逻辑 ==================

    public void getFridgeData() {
        if (device.getDeviceData() == null) {
            fridgeTemp.setValue(null);
            freezeTemp.setValue(null);
            return;
        }
        fridgeTemp.setValue(Float.valueOf(device.getDeviceData().get("fridgeTemp")));
        freezeTemp.setValue(Float.valueOf(device.getDeviceData().get("freezeTemp")));
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        setFridgeTemp.setValue(deviceDataManager.getFridgeTemp());
        setFreezeTemp.setValue(deviceDataManager.getFreezeTemp());
        if (deviceDataManager.getDeviceMode() == null) {
            selectedMode.setValue("标准");
        } else {
            selectedMode.setValue(deviceDataManager.getDeviceMode());
        }
    }


    // ================== 微波炉业务逻辑 ==================

    public void setMicrowaveTime(int seconds) {
        // 格式化时间显示 MM:ss
        String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public void startMicrowave(int seconds, String powerLevel) {
        if (seconds <= 0) {
            return;
        }
        Log.d(TAG, "Network: Start Microwave " + seconds + "s at " + powerLevel);
    }

// ================== 电饭煲业务逻辑 ==================


// ================== 消毒柜业务逻辑 ==================

// ================== 洗碗机业务逻辑 ==================

    // ================== 设备通用业务逻辑 ==================
    public LiveData<Device> getDevice() {
        deviceRepository = new DeviceRepository(getApplication());
        return deviceRepository.getDevice(device.getDeviceSn());
    }

    public void initDevice(Device device) {
        this.device = device;
        deviceDataManager = DeviceDataManager.Instance(getApplication(), device.getDeviceSn());
        setDeviceImage(this.device.getDeviceStatus(), this.device.getDeviceType(), this.device.getDeviceName());
        setDeviceState(this.device.getDeviceStatus());
        setChipDeviceMode(this.device.getDeviceType());
        //setDeviceSet();
        switch (this.device.getDeviceType()) {
            case "REFRIGERATOR": // 冰箱
                getFridgeData();
                break;
            case "MICROWAVE": // 微波炉
                break;
            case "DISHWASHER": // 洗碗机
                break;
            case "RICE_COOKER": // 电饭煲
                break;
            case "STERILIZER": // 消毒柜
                break;
        }
    }

    public void setDeviceImage(String deviceState, String deviceType, String deviceName) {
        this.deviceName.setValue(deviceName);
        if (deviceState != null && deviceState.equals("OFFLINE")) {
            deviceImage.setValue(ContextCompat.getDrawable(getApplication(), Objects.requireNonNull(DeviceType.fromName(deviceType)).getOffline()));
        } else {
            deviceImage.setValue(ContextCompat.getDrawable(getApplication(), Objects.requireNonNull(DeviceType.fromName(deviceType)).getOnline()));
        }
    }

    public void setDeviceState(String deviceState) {
        if (deviceState != null) {
            switch (deviceState) {
                case "ONLINE":
                    status.setValue("在线");
                    statusColor.setValue(R.color.state_info);
                    break;
                case "OFFLINE":
                    status.setValue("离线");
                    statusColor.setValue(R.color.state_disabled);
                    break;
                default:
                    status.setValue("未知");
                    statusColor.setValue(R.color.state_error);
                    break;
            }
        }
    }

    public void setChipDeviceMode(String deviceType) {
        if (deviceType.equals(DeviceType.REFRIGERATOR.name())) {
            modeTags.setValue(ChipDeviceMode.REFRIGERATOR_MODES);
        } else if (deviceType.equals(DeviceType.MICROWAVE.name())) {
            modeTags.setValue(ChipDeviceMode.MICROWAVE_MODES);
        } else if (deviceType.equals(DeviceType.RICE_COOKER.name())) {
            modeTags.setValue(ChipDeviceMode.RICE_COOKER_MODES);
        } else if (deviceType.equals(DeviceType.DISHWASHER.name())) {
            modeTags.setValue(ChipDeviceMode.DISHWASHER_MODES);
        } else if (deviceType.equals(DeviceType.STERILIZER.name())) {
            modeTags.setValue(ChipDeviceMode.STERILIZER_MODES);
        }
    }

    public void setDeviceSet() {
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            setFridgeTemp.setValue(deviceDataManager.getFridgeTemp());
            setFreezeTemp.setValue(deviceDataManager.getFreezeTemp());
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
        }
    }

    public void saveDataGeneral() {
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveFridgeSet(setFridgeTemp.getValue(), setFreezeTemp.getValue());
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
        }
    }


    public void submitTask() {
        subTaskLoading.setValue(true);
        Map<String, String> data = new HashMap<>();
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceSn", device.getDeviceSn());
        payload.put("mode", DeviceMode.toMode(selectedMode.getValue()));
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            data.put("fridgeTempThreshold", String.valueOf(setFridgeTemp.getValue()));
            data.put("freezeTempThreshold", String.valueOf(setFreezeTemp.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
        }
        payload.put("data", JsonUtils.toJson(data));
        deviceRepository.sendControlCmd(getApplication(), payload, new DeviceRepository.callback() {
            @Override
            public void onSuccess(String message) {
                subTaskLoading.setValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                subTaskLoading.setValue(false);
                Toast.makeText(getApplication(), "指令下发 错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
