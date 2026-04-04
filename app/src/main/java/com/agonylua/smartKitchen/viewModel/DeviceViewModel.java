package com.agonylua.smartKitchen.viewModel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.model.ChipDeviceMode;
import com.agonylua.smartKitchen.model.DeviceMode;
import com.agonylua.smartKitchen.model.DeviceSet;
import com.agonylua.smartKitchen.model.DeviceType;
import com.agonylua.smartKitchen.repository.DeviceRepository;
import com.agonylua.smartKitchen.utils.DeviceDataManager;
import com.agonylua.smartKitchen.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class DeviceViewModel extends ViewModel {
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
    // ================== 冰箱专用 ==================
    public final MutableLiveData<Float> fridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> freezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    public final MutableLiveData<Float> setFridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> setFreezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    // ================== 微波炉专用 ==================
    public final MutableLiveData<Float> setMicrowaveTime = new MutableLiveData<>(20.0f); // 默认 1分30秒
    public final MutableLiveData<Float> setMicrowaveTemp = new MutableLiveData<>(100.0f); // 默认 100℃
    public final MutableLiveData<String> microwaveTimeDisplay = new MutableLiveData<>("--:--");
    public final MutableLiveData<Integer> microwaveTime = new MutableLiveData<>();
    public final MutableLiveData<Integer> microwaveTotalTime = new MutableLiveData<>();
    public final MutableLiveData<String> microwaveMode = new MutableLiveData<>("加热模式");
    // ================= 洗碗机专用 ==================
    public final MutableLiveData<Boolean> dishwasherSalt = new MutableLiveData<>(); // 洗碗机盐状态数据
    public final MutableLiveData<String> dishwasherStatus = new MutableLiveData<>(); // 洗碗机状态数据
    public final MutableLiveData<Boolean> dishwasherRinseAid = new MutableLiveData<>(); // 漂洗剂状态数据
    public final MutableLiveData<Boolean> dishwasherKeepFresh = new MutableLiveData<>(false); // 保鲜状态数据
    // ================= 电饭煲专用 ==================
    public final MutableLiveData<String> riceCookerTexture = new MutableLiveData<>("适中");// 口感调节数据
    public final MutableLiveData<Boolean> riceCookerInsulation = new MutableLiveData<>();// 保温状态数据
    public final MutableLiveData<String> riceCookerTime = new MutableLiveData<>();// 电饭煲时间数据
    public final MutableLiveData<String> riceCookerStatus = new MutableLiveData<>();// 电饭煲时间数据
    public final MutableLiveData<Integer> riceCookerProgress = new MutableLiveData<>(0);// 电饭煲进度数据
    public final MutableLiveData<Integer> riceCookerTotalTime = new MutableLiveData<>();// 电饭煲总时间数据
    // ================= 消毒柜专用 ==================
    public final MutableLiveData<String> sterilizerStatus = new MutableLiveData<>("待机");
    public final MutableLiveData<Integer> sterilizerTimeMinutes = new MutableLiveData<>(90);
    public final MutableLiveData<Integer> sterilizerTimeDisplay = new MutableLiveData<>();
    public final MutableLiveData<Integer> sterilizerTemp = new MutableLiveData<>(25);
    public final MutableLiveData<Integer> sterilizerProgress = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> sterilizerUvLight = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> sterilizerTotalTime = new MutableLiveData<>();
    // 聚合观察者
    public MediatorLiveData<Boolean> autoSaverData = new MediatorLiveData<>();
    public MediatorLiveData<Boolean> autoSaverSetup = new MediatorLiveData<>();
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private Device device;
    private DeviceSet deviceSet;
    private DeviceDataManager deviceDataManager;
    private DeviceRepository deviceRepository;
    private Context context;

    @Inject
    public DeviceViewModel(@ApplicationContext Context context, DeviceRepository deviceRepository) {
        this.context = context;
        this.deviceRepository = deviceRepository;
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
    public void getMicrowaveData() {
        if (device.getDeviceData() == null) {
            microwaveTimeDisplay.setValue(null);
            setMicrowaveTime.setValue(10.0f);
            setMicrowaveTemp.setValue(30.0f);
            if (deviceDataManager.getDeviceMode() == null) {
                selectedMode.setValue("加热");
            } else {
                selectedMode.setValue(deviceDataManager.getDeviceMode());
            }
            return;
        }
        int minutes = Integer.parseInt(device.getDeviceData().get("microwaveTime")) / 60;
        int seconds = Integer.parseInt(device.getDeviceData().get("microwaveTime")) % 60;
        microwaveTimeDisplay.setValue(String.format("%02d:%02d", minutes, seconds));
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        microwaveTime.setValue(Integer.parseInt(device.getDeviceData().get("microwaveTime")));
        setMicrowaveTime.setValue(deviceDataManager.getMicrowaveTime());
        setMicrowaveTemp.setValue(deviceDataManager.getMicrowaveTemp());
        if (deviceDataManager.getDeviceMode() == null) {
            selectedMode.setValue("加热");
        } else {
            selectedMode.setValue(deviceDataManager.getDeviceMode());
        }

        switch (device.getDeviceMode()) {
            case "HEAT":
                microwaveTotalTime.setValue(90);
                break;
            case "GRILL":
                microwaveTotalTime.setValue(120);
                break;
            case "DEFROST":
                microwaveTotalTime.setValue(60);
                break;
            case "STEAM":
                microwaveTotalTime.setValue(150);
                break;
        }
    }

    // 处理 "+30s" 按钮的逻辑
    public void addMicrowaveTime(float addSeconds) {
        float currentTime = microwaveTotalTime.getValue() != null ? microwaveTotalTime.getValue() : 0.0f;
        float newTime = currentTime + addSeconds;
        setMicrowaveTime.setValue(newTime);
        int minutes = (int) newTime / 60;
        int seconds = (int) newTime % 60;
        microwaveTime.setValue((int) newTime);
        microwaveTimeDisplay.setValue(String.format("%02d:%02d", minutes, seconds));
    }


    // ================== 电饭煲业务逻辑 ==================
    public void getRiceCookerData() {
        if (device.getDeviceData() == null) {
            riceCookerTexture.setValue("适中");
            riceCookerStatus.setValue("--");
            riceCookerProgress.setValue(0);
            if (deviceDataManager.getDeviceMode() == null) {
                selectedMode.setValue("精煮饭");
            } else {
                selectedMode.setValue(deviceDataManager.getDeviceMode());
            }
            return;
        }
        changeRiceCookerMode(deviceMode.getValue());
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        riceCookerTime.setValue(device.getDeviceData().get("remainTime"));
        riceCookerTexture.setValue(deviceDataManager.getRiceCookerTexture());
        riceCookerInsulation.setValue(deviceDataManager.getRiceCookerInsulation());
        riceCookerStatus.setValue(riceCookerInsulation.getValue() ? "保温中" : "烹饪中");
        if (deviceDataManager.getDeviceMode() == null) {
            selectedMode.setValue("精煮饭");
        } else {
            selectedMode.setValue(deviceDataManager.getDeviceMode());
        }
    }

    public void setRiceCookerTexture(String texture) {
        riceCookerTexture.setValue(texture);
    }

    public void changeRiceCookerMode(String mode) {
        int time = 0; // 默认
        if (DeviceMode.COOK_RICE.name().equals(mode)) time = 25;
        else if (DeviceMode.STEAM_COOK.name().equals(mode)) time = 45;
        else if (DeviceMode.PORRIDGE.name().equals(mode)) time = 60;
        else if (DeviceMode.CAKE.name().equals(mode)) time = 30;

        riceCookerTotalTime.setValue(time);
        riceCookerProgress.setValue(Integer.parseInt(device.getDeviceData().get("progress")));
    }

    // ================== 消毒柜业务逻辑 ==================
    public void getSterilizerData() {
        if (device.getDeviceData() == null) return;

        if (device.getDeviceData().containsKey("temp")) {
            sterilizerTemp.setValue(Integer.parseInt(device.getDeviceData().get("temp")));
        }
        if (device.getDeviceData().containsKey("time")) {
            sterilizerTimeDisplay.setValue(Integer.parseInt(device.getDeviceData().get("time")));
        }
        if (device.getDeviceData().containsKey("progress")) {
            sterilizerProgress.setValue(Integer.parseInt(device.getDeviceData().get("progress")));
        }

        sterilizerStatus.setValue(device.getDeviceStatus());
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        sterilizerUvLight.setValue(deviceDataManager.getSterilizerUVLight());
        selectedMode.setValue(deviceDataManager.getDeviceMode() != null ? deviceDataManager.getDeviceMode() : "自动");
    }

    /**
     * 切换模式，并自动设置对应的默认时长
     */
    public void setSterilizerMode(String mode) {
        selectedMode.setValue(mode);
        int time = 0;
        if (DeviceMode.UVB.name().equals(mode)) time = 60;
        else if (DeviceMode.DRY.name().equals(mode)) time = 45;
        else if (DeviceMode.AUTO.name().equals(mode)) time = 90;
        else if (DeviceMode.HIGH_TEMP.name().equals(mode)) time = 120;

        sterilizerTotalTime.setValue(time);
    }

    // ================== 洗碗机业务逻辑 ==================
    public void getDishwasherData() {
        if (device.getDeviceData() == null) return;

        if (device.getDeviceData().containsKey("salt")) {
            dishwasherSalt.setValue(Objects.equals(device.getDeviceData().get("salt"), "true"));
        }
        if (device.getDeviceData().containsKey("rinseAid")) {
            dishwasherRinseAid.setValue(Objects.equals(device.getDeviceData().get("rinseAid"), "true"));
        }
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        dishwasherKeepFresh.setValue(deviceDataManager.getDishwasherKeepFresh());
        selectedMode.setValue(deviceDataManager.getDeviceMode());
    }

    public void setDishwasherMode(String mode) {
        selectedMode.setValue(mode);
    }

    // ================== 设备通用业务逻辑 ==================
    public LiveData<Device> getDevice() {
        return deviceRepository.getDevice(device.getDeviceSn());
    }

    public void initDevice(Device device) {
        this.device = device;
        deviceDataManager = DeviceDataManager.Instance((android.app.Application) context.getApplicationContext(), device.getDeviceSn());
        setDeviceImage(this.device.getDeviceStatus(), this.device.getDeviceType(), this.device.getDeviceName());
        setDeviceState(this.device.getDeviceStatus());
        setChipDeviceMode(this.device.getDeviceType());
        switch (this.device.getDeviceType()) {
            case "REFRIGERATOR": // 冰箱
                getFridgeData();
                break;
            case "MICROWAVE": // 微波炉
                getMicrowaveData();
                break;
            case "DISHWASHER": // 洗碗机
                getDishwasherData();
                break;
            case "RICE_COOKER": // 电饭煲
                getRiceCookerData();
                break;
            case "STERILIZER": // 消毒柜
                getSterilizerData();
                break;
        }
    }

    public void setDeviceImage(String deviceState, String deviceType, String deviceName) {
        this.deviceName.setValue(deviceName);
        if (deviceState != null && deviceState.equals("OFFLINE")) {
            deviceImage.setValue(ContextCompat.getDrawable(context, Objects.requireNonNull(DeviceType.fromName(deviceType)).getOffline()));
        } else {
            deviceImage.setValue(ContextCompat.getDrawable(context, Objects.requireNonNull(DeviceType.fromName(deviceType)).getOnline()));
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
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            riceCookerTexture.setValue(deviceDataManager.getRiceCookerTexture());
        }
    }

    public void saveDataGeneral() {
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveFridgeSet(setFridgeTemp.getValue(), setFreezeTemp.getValue());
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveMicrowaveSet(setMicrowaveTime.getValue(), setMicrowaveTemp.getValue());
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveDishwasherKeepFresh(dishwasherKeepFresh.getValue());
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveRiceCookerSet(riceCookerTexture.getValue());
            deviceDataManager.saveRiceCookerInsulation(riceCookerInsulation.getValue());
        } else if (Objects.equals(device.getDeviceType(), "STERILIZER")) {
            deviceDataManager.setDeviceMode(selectedMode.getValue());
            deviceDataManager.saveSterilizerUVLight(sterilizerUvLight.getValue());
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
            data.put("microwaveTime", String.valueOf(setMicrowaveTime.getValue()));
            data.put("microwaveTemp", String.valueOf(setMicrowaveTemp.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            data.put("riceCookerTexture", riceCookerTexture.getValue());
        } else if (Objects.equals(device.getDeviceType(), "STERILIZER")) {
        }
        payload.put("data", JsonUtils.toJson(data));
        deviceRepository.sendControlCmd(context, payload, new DeviceRepository.callback() {
            @Override
            public void onSuccess(String message) {
                subTaskLoading.postValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                subTaskLoading.postValue(false);
                toastMessage.postValue("指令下发 错误");
            }
        });
    }
}
