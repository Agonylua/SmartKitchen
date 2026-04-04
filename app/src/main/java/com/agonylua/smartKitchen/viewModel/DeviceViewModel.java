package com.agonylua.smartKitchen.viewModel;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.model.ChipDeviceMode;
import com.agonylua.smartKitchen.model.DeviceMode;
import com.agonylua.smartKitchen.model.DeviceSet;
import com.agonylua.smartKitchen.model.DeviceType;
import com.agonylua.smartKitchen.repository.DeviceRepository;
import com.agonylua.smartKitchen.utils.DeviceDataManager;
import com.agonylua.smartKitchen.utils.JsonUtils;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;

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
    public final MutableLiveData<Boolean> statusColor = new MutableLiveData<>();// 设备状态颜色数据
    public final MutableLiveData<Drawable> deviceImage = new MutableLiveData<>();// 设备图片数据
    public final MutableLiveData<String> deviceName = new MutableLiveData<>();// 设备名称数据
    public final MutableLiveData<String> deviceMode = new MutableLiveData<>();// 设备模式数据
    public final MutableLiveData<List<String>> modeTags = new MutableLiveData<>(); // 模式标签数据
    public final MutableLiveData<Boolean> statusLoading = new MutableLiveData<>();// 加载状态数据
    public final MutableLiveData<Boolean> subTaskLoading = new MutableLiveData<>(false);// 加载状态数据
    // ================== 冰箱专用 ==================
    public final MutableLiveData<String> refrigeratorSelectedMode = new MutableLiveData<>(); // 冰箱选中模式标签数据
    public final MutableLiveData<Float> fridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> freezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    public final MutableLiveData<Float> setFridgeTemp = new MutableLiveData<>();// 冰箱温度数据
    public final MutableLiveData<Float> setFreezeTemp = new MutableLiveData<>();// 冰箱湿度数据
    // ================== 微波炉专用 ==================
    public final MutableLiveData<String> microwaveSelectedMode = new MutableLiveData<>(); // 微波炉选中模式标签数据
    public final MutableLiveData<Float> setMicrowaveTime = new MutableLiveData<>(20.0f); // 默认 1分30秒
    public final MutableLiveData<Float> setMicrowaveTemp = new MutableLiveData<>(100.0f); // 默认 100℃
    public final MutableLiveData<String> microwaveTimeDisplay = new MutableLiveData<>("--:--");
    public final MutableLiveData<Integer> microwaveTime = new MutableLiveData<>();
    public final MutableLiveData<Integer> microwaveTotalTime = new MutableLiveData<>();
    public final MutableLiveData<Integer> microwaveProgress = new MutableLiveData<>(0);
    // ================= 洗碗机专用 ==================
    public final MutableLiveData<String> dishwasherSelectedMode = new MutableLiveData<>(); // 洗碗机选中模式标签数据
    public final MutableLiveData<Boolean> dishwasherSalt = new MutableLiveData<>(); // 洗碗机盐状态数据
    public final MutableLiveData<String> dishwasherStatus = new MutableLiveData<>(); // 洗碗机状态数据
    public final MutableLiveData<Boolean> dishwasherRinseAid = new MutableLiveData<>(); // 漂洗剂状态数据
    public final MutableLiveData<Boolean> dishwasherKeepFresh = new MutableLiveData<>(false); // 保鲜状态数据
    // ================= 电饭煲专用 ==================
    public final MutableLiveData<String> riceCookerSelectedMode = new MutableLiveData<>(); // 电饭煲选中模式标签数据
    public final MutableLiveData<Integer> riceCookerTexture = new MutableLiveData<>(2);// 口感调节数据
    public final MutableLiveData<Boolean> riceCookerInsulation = new MutableLiveData<>();// 保温状态数据
    public final MutableLiveData<String> riceCookerTime = new MutableLiveData<>();// 电饭煲时间数据
    public final MutableLiveData<String> riceCookerStatus = new MutableLiveData<>();// 电饭煲时间数据
    public final MutableLiveData<Integer> riceCookerProgress = new MutableLiveData<>(0);// 电饭煲进度数据
    public final MutableLiveData<Integer> riceCookerTotalTime = new MutableLiveData<>();// 电饭煲总时间数据
    // ================= 消毒柜专用 ==================
    public final MutableLiveData<String> sterilizerSelectedMode = new MutableLiveData<>(); // 电饭煲选中模式标签数据
    public final MutableLiveData<String> sterilizerStatus = new MutableLiveData<>("待机");
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
        autoSaverSetup.addSource(refrigeratorSelectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(microwaveSelectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(dishwasherSelectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(riceCookerSelectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(sterilizerSelectedMode, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setFridgeTemp, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setFreezeTemp, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setMicrowaveTemp, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(setMicrowaveTime, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(dishwasherKeepFresh, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(riceCookerTexture, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(riceCookerInsulation, value -> autoSaverSetup.setValue(true));
        autoSaverSetup.addSource(sterilizerUvLight, value -> autoSaverSetup.setValue(true));
    }

    // ================== 冰箱业务逻辑 ==================

    public void getFridgeData() {
        fridgeTemp.setValue(Float.valueOf(device.getDeviceData().get("fridgeTemp")));
        freezeTemp.setValue(Float.valueOf(device.getDeviceData().get("freezeTemp")));
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        setFridgeTemp.setValue(deviceDataManager.getFridgeTemp());
        setFreezeTemp.setValue(deviceDataManager.getFreezeTemp());
        refrigeratorSelectedMode.setValue(deviceDataManager.getFridgeMode());
    }


    // ================== 微波炉业务逻辑 ==================
    public void getMicrowaveData() {
        int minutes = Integer.parseInt(device.getDeviceData().get("microwaveTime")) / 60;
        int seconds = Integer.parseInt(device.getDeviceData().get("microwaveTime")) % 60;
        microwaveTimeDisplay.setValue(String.format("%02d:%02d", minutes, seconds));
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        microwaveTime.setValue(Integer.parseInt(device.getDeviceData().get("microwaveTime")));
        setMicrowaveTime.setValue(deviceDataManager.getMicrowaveTime());
        setMicrowaveTemp.setValue(deviceDataManager.getMicrowaveTemp());
        microwaveSelectedMode.setValue(deviceDataManager.getMicrowaveMode());
        microwaveProgress.setValue(Integer.parseInt(device.getDeviceData().get("progress")));

    }

    public String formatMicrowaveTime(float totalSeconds) {
        int minutes = (int) totalSeconds / 60;
        int seconds = (int) totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    // ================== 电饭煲业务逻辑 ==================
    public void getRiceCookerData() {
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        riceCookerTime.setValue(device.getDeviceData().get("remainTime"));
        riceCookerTexture.setValue(deviceDataManager.getRiceCookerTexture());
        riceCookerInsulation.setValue(deviceDataManager.getRiceCookerInsulation());
        riceCookerSelectedMode.setValue(deviceDataManager.getRiceCookerMode());
        riceCookerProgress.setValue(Integer.parseInt(device.getDeviceData().get("progress")));
    }

    public void setRiceCookerTexture(int texture) {
        riceCookerTexture.setValue(texture);
    }

    // ================== 消毒柜业务逻辑 ==================
    public void getSterilizerData() {

        sterilizerTemp.setValue(Integer.parseInt(device.getDeviceData().get("temp")));
        sterilizerTimeDisplay.setValue(Integer.parseInt(device.getDeviceData().get("time")));
        sterilizerProgress.setValue(Integer.parseInt(device.getDeviceData().get("progress")));

        sterilizerStatus.setValue(device.getDeviceStatus());
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        sterilizerUvLight.setValue(deviceDataManager.getSterilizerUVLight());
        sterilizerSelectedMode.setValue(deviceDataManager.getSterilizerMode());
    }

    public void setSterilizerMode(String mode) {
        sterilizerSelectedMode.setValue(mode);
    }

    // ================== 洗碗机业务逻辑 ==================
    public void getDishwasherData() {

        dishwasherSalt.setValue(Objects.equals(device.getDeviceData().get("salt"), "true"));
        dishwasherRinseAid.setValue(Objects.equals(device.getDeviceData().get("rinseAid"), "true"));
        deviceMode.setValue(DeviceMode.toLabel(device.getDeviceMode()));
        dishwasherKeepFresh.setValue(deviceDataManager.getDishwasherKeepFresh());
        dishwasherSelectedMode.setValue(deviceDataManager.getDishwasherMode());
    }

    public void setDishwasherMode(String mode) {
        dishwasherSelectedMode.setValue(mode);
    }

    // ================== 设备通用业务逻辑 ==================
    public LiveData<Device> getDevice() {
        return deviceRepository.getDevice(device.getDeviceSn());
    }

    public void initDevice(Device device) {
        this.device = device;
        deviceDataManager = DeviceDataManager.Instance((android.app.Application) context.getApplicationContext(), device.getDeviceSn());
        setDeviceImage(this.device.getDeviceStatus(), this.device.getDeviceType(), this.device.getDeviceName());
        if (device.getDeviceStatus().equals("OFFLINE")) {
            status.setValue("离线");
            statusColor.setValue(false);
        } else {
            status.setValue("在线");
            statusColor.setValue(true);
        }
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
            setMicrowaveTime.setValue(deviceDataManager.getMicrowaveTime());
            setMicrowaveTemp.setValue(deviceDataManager.getMicrowaveTemp());
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
            dishwasherKeepFresh.setValue(deviceDataManager.getDishwasherKeepFresh());
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            riceCookerTexture.setValue(deviceDataManager.getRiceCookerTexture());
        }
    }

    public MutableLiveData<String> getSelectedModeLiveData() {
        if (device == null || device.getDeviceType() == null) {
            return new MutableLiveData<>("");
        }
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            return refrigeratorSelectedMode;
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
            return microwaveSelectedMode;
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
            return dishwasherSelectedMode;
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            return riceCookerSelectedMode;
        } else if (Objects.equals(device.getDeviceType(), "STERILIZER")) {
            return sterilizerSelectedMode;
        }
        return new MutableLiveData<>("");
    }

    public void saveDataGeneral() {
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            deviceDataManager.setFridgeMode(refrigeratorSelectedMode.getValue());
            deviceDataManager.saveFridgeSet(setFridgeTemp.getValue(), setFreezeTemp.getValue());
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
            deviceDataManager.setMicrowaveMode(microwaveSelectedMode.getValue());
            deviceDataManager.saveMicrowaveSet(setMicrowaveTime.getValue(), setMicrowaveTemp.getValue());
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
            deviceDataManager.setDishwasherMode(dishwasherSelectedMode.getValue());
            deviceDataManager.saveDishwasherKeepFresh(dishwasherKeepFresh.getValue());
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            deviceDataManager.setRiceCookerMode(riceCookerSelectedMode.getValue());
            deviceDataManager.saveRiceCookerSet(riceCookerTexture.getValue());
            deviceDataManager.saveRiceCookerInsulation(riceCookerInsulation.getValue());
        } else if (Objects.equals(device.getDeviceType(), "STERILIZER")) {
            deviceDataManager.setSterilizerMode(sterilizerSelectedMode.getValue());
            deviceDataManager.saveSterilizerUVLight(sterilizerUvLight.getValue());
        }
    }


    public void submitTask() {
        subTaskLoading.setValue(true);
        Map<String, String> data = new HashMap<>();
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceSn", device.getDeviceSn());
        payload.put("mode", DeviceMode.toMode(getSelectedModeLiveData().getValue()));
        if (Objects.equals(device.getDeviceType(), "REFRIGERATOR")) {
            data.put("fridgeTempThreshold", String.valueOf(setFridgeTemp.getValue()));
            data.put("freezeTempThreshold", String.valueOf(setFreezeTemp.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "MICROWAVE")) {
            data.put("microwaveTime", String.valueOf(setMicrowaveTime.getValue()));
            data.put("microwaveTemp", String.valueOf(setMicrowaveTemp.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "DISHWASHER")) {
            data.put("keepFresh", String.valueOf(dishwasherKeepFresh.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "RICE_COOKER")) {
            data.put("riceCookerTexture", String.valueOf(riceCookerTexture.getValue()));
            data.put("riceCookerInsulation", String.valueOf(riceCookerInsulation.getValue()));
        } else if (Objects.equals(device.getDeviceType(), "STERILIZER")) {
            data.put("uvLight", String.valueOf(sterilizerUvLight.getValue()));
        }
        payload.put("data", JsonUtils.toJson(data));
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            deviceRepository.sendControlCmd(payload, new DeviceRepository.callback() {
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
        }, 2000);
    }

    public void updateDeviceStatus(String deviceSn) {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            deviceRepository.updateDeviceStatus(deviceSn, new DeviceRepository.callback() {
                @Override
                public void onSuccess(String message) {
                    toastMessage.postValue(message);
                    statusLoading.postValue(false);
                    if (Objects.equals(status.getValue(), "离线")) {
                        status.postValue("在线");
                        statusColor.postValue(true);
                    } else {
                        status.postValue("离线");
                        statusColor.postValue(false);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    toastMessage.postValue(errorMessage);
                    statusLoading.postValue(false);
                }
            });
        }, 2000);
    }
}
