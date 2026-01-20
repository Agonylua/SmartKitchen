package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.model.DeviceMode;
import com.agonylua.smarthome.utils.DeviceDataManager;
import com.agonylua.smarthome.utils.JsonUtils;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

public class DeviceViewModel extends AndroidViewModel {
    private static final String TAG = "DeviceViewModel";
    private final DeviceDao deviceDao;
    //----------------- LiveData 定义 -----------------
    // 设备公共部分
    private final MutableLiveData<String> state = new MutableLiveData<>();// 设备状态数据
    private final MutableLiveData<Integer> state_Color = new MutableLiveData<>();// 设备状态颜色数据
    private final MutableLiveData<Integer> device_Image = new MutableLiveData<>();// 设备图片数据
    private final MutableLiveData<String> device_Name = new MutableLiveData<>();// 设备电源数据
    private final MutableLiveData<Integer> device_Mode = new MutableLiveData<>(3);// 设备模式数据
    // 冰箱
    private final MutableLiveData<String> fridge_temp = new MutableLiveData<>();// 冰箱温度数据
    private final MutableLiveData<String> fridge_hum = new MutableLiveData<>();// 冰箱湿度数据
    private final MutableLiveData<Float> limit_fridge_temp = new MutableLiveData<>();// 冰箱温度数据
    private final MutableLiveData<Float> limit_fridge_hum = new MutableLiveData<>();// 冰箱湿度数据
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
    private Device device;
    private DeviceDataManager deviceDataManager;

    public DeviceViewModel(@NonNull Application application) {
        super(application);
        deviceDao = AppDatabase.getInstance(getApplication()).deviceDao();
    }

    // ================== 冰箱业务逻辑 ==================

    public void getFridgeData(String deviceName, String deviceState) {
        ThreadPoolUtils.getInstance().execute(() -> {
            device_Name.postValue(deviceName);
            String json = deviceDao.getDeviceDataByName(deviceName);
            Map<String, String> deviceData = JsonUtils.toMap(json);
            if (deviceData != null) {
                String tempStr = deviceData.get("temp");
                String humStr = deviceData.get("hum");
                fridge_temp.postValue(tempStr);
                fridge_hum.postValue(humStr);
            }
        });
        deviceDataManager = DeviceDataManager.Instance(getApplication(), deviceName);
        limit_fridge_temp.setValue(deviceDataManager.getFridgeTemp());
        limit_fridge_hum.setValue(deviceDataManager.getFridgeHum());
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
    public void DeviceGeneral(String deviceState) {
        switch (deviceState) {
            case "IDLE":
                state.postValue("空闲");
                state_Color.postValue(R.color.state_info);
                break;
            case "RUNNING":
                state.postValue("运行中");
                state_Color.postValue(R.color.state_success);
                break;
            case "OFFLINE":
                state.postValue("离线");
                state_Color.postValue(R.color.state_disabled);
                break;
            default:
                state.postValue("状态异常");
                state_Color.postValue(R.color.state_error);
                break;
        }

    }

    public void saveDataGeneral(String deviceType, String deviceName) {
        Integer currentMode = device_Mode.getValue();
        if (currentMode == null) {
            currentMode = 1;
        }
        Log.d(TAG, "saveDataGeneral: " + currentMode);
        deviceDataManager = DeviceDataManager.Instance(getApplication(), deviceName);
        if (Objects.equals(deviceType, "REFRIGERATOR")) {
            deviceDataManager.saveFridgeSet(limit_fridge_temp.getValue(), limit_fridge_hum.getValue());
            switch (currentMode) {
                case 1:
                    deviceDataManager.setDeviceMode(DeviceMode.REFRIGERATOR_MODE_RAPID_COOL);
                    break;
                case 2:
                    deviceDataManager.setDeviceMode(DeviceMode.REFRIGERATOR_MODE_ENERGY_SAVE);
                    break;
                case 3:
                    deviceDataManager.setDeviceMode(DeviceMode.REFRIGERATOR_MODE_HOLIDAY);
                    break;
                case 0:
                default:
                    deviceDataManager.setDeviceMode(DeviceMode.REFRIGERATOR_MODE_STANDARD);
                    break;
            }
        } else if (Objects.equals(deviceType, "MICROWAVE")) {
        } else if (Objects.equals(deviceType, "DISHWASHER")) {
        }
    }

    // ================== Getter ==================
    public LiveData<String> getDeviceName() {
        return device_Name;
    }

    public LiveData<String> getState() {
        return state;
    }


    public MutableLiveData<Integer> getDeviceMode() {
        return device_Mode;
    }

    public LiveData<String> getFridgeTemp() {
        return fridge_temp;
    }

    public LiveData<String> getFridgeHum() {
        return fridge_hum;
    }

    public MutableLiveData<Float> getSetFridgeTemp() {
        return limit_fridge_temp;
    }

    public MutableLiveData<Float> getSetFridgeHum() {
        return limit_fridge_hum;
    }

    public MutableLiveData<Integer> getState_Color() {
        return state_Color;
    }

    public MutableLiveData<Integer> getDevice_Image() {
        return device_Image;
    }
}
