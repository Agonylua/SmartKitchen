package com.agonylua.smarthome.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.database.entity.Rules;
import com.agonylua.smarthome.dto.AutomationRuleDTO;
import com.agonylua.smarthome.dto.RuleAction;
import com.agonylua.smarthome.dto.RuleCondition;
import com.agonylua.smarthome.model.DeviceMode;
import com.agonylua.smarthome.model.DeviceStatus;
import com.agonylua.smarthome.repository.SmartRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

public class SmartViewModel extends ViewModel {
    private static final String TAG = "SmartViewModel";
    // --- 基础规则信息 ---
    public final MutableLiveData<String> ruleName = new MutableLiveData<>("新建自动化规则");
    // --- 触发条件 (IF) 类型控制 ---
    // 0 = 时间触发, 1 = 传感器触发, 2 = 设备状态触发
    public final MutableLiveData<Integer> selectedConditionType = new MutableLiveData<>(1);
    // [类型0: 时间触发] 绑定的字段
    public final MutableLiveData<String> selectedTime = new MutableLiveData<>("08:00");
    public final MutableLiveData<String> selectedDays = new MutableLiveData<>("每天"); // 简化处理，实际可存List
    // [类型1: 传感器触发] 绑定的字段
    public final MutableLiveData<String> selectedSensorName = new MutableLiveData<>("");
    public final MutableLiveData<Float> sensorThreshold = new MutableLiveData<>(30.0f);
    // [类型2: 设备状态触发] 绑定的字段
    public final MutableLiveData<String> selectedCondDeviceName = new MutableLiveData<>("");
    public final MutableLiveData<String> selectedCondStateName = new MutableLiveData<>("");
    // --- 执行动作 (THEN) 绑定的字段 ---
    public final MutableLiveData<String> selectedActionDeviceName = new MutableLiveData<>("");
    public final MutableLiveData<String> selectedActionCommandName = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> saveRuleResult = new MutableLiveData<>();
    // --- 下拉列表数据源 ---
    private final MutableLiveData<List<String>> sensorList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> deviceList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> condStateList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> actionCommandList = new MutableLiveData<>();
    private LiveData<List<Rules>> rulesList = new MutableLiveData<>();
    private SmartRepository repository;

    public void init(SmartRepository repository) {
        this.repository = repository;
        loadMockData();
        syncRulesList();
    }

    public void syncRulesList() {
        repository.getRules(new SmartRepository.SmartCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    /**
     * 【新增辅助方法】在后台线程中同步获取设备，解决 LiveData.getValue() 为 null 的致命问题
     */
    private Device findDeviceByNameSync(String deviceName) {
        if (deviceName == null) return null;
        List<Device> devices = repository.getDeviceList(); // 此方法直接查库，必须在子线程运行
        if (devices != null) {
            for (Device device : devices) {
                if (deviceName.equals(device.getDeviceName())) {
                    return device;
                }
            }
        }
        return null;
    }

    private void loadMockData() {
        List<String> sensors = new ArrayList<>();
        sensors.add("环境湿度");
        sensors.add("环境温度");
        sensors.add("总功耗");
        sensorList.setValue(sensors);
    }

    /**
     * [执行]触发条件
     */
    public void updateStatesForConditionDevice(String deviceName) {
        ThreadPoolUtils.getInstance().execute(() -> {
            Device device = findDeviceByNameSync(deviceName);
            if (device == null) return;
            List<String> states = new ArrayList<>();
            states.add(DeviceStatus.ONLINE.getState());
            states.add(DeviceStatus.OFFLINE.getState());
            if (device.getDeviceType() != null) {
                switch (device.getDeviceType()) {
                    case "REFRIGERATOR":
                        states.add(DeviceMode.STANDARD.getLabel());
                        states.add(DeviceMode.FAST_COOL.getLabel());
                        states.add(DeviceMode.ENERGY_SAVING.getLabel());
                        states.add(DeviceMode.HOLIDAY.getLabel());
                        break;
                    case "DISHWASHER":
                        states.add(DeviceMode.IDLE.getLabel());
                        states.add(DeviceMode.STANDARD_WASH.getLabel());
                        states.add(DeviceMode.QUICK_WASH.getLabel());
                        states.add(DeviceMode.INTENSIVE_WASH.getLabel());
                        states.add(DeviceMode.ECO_WASH.getLabel());
                        states.add(DeviceMode.SANITIZE_WASH.getLabel());
                        states.add(DeviceMode.DRY.getLabel());
                    case "RICE_COOKER":
                        states.add(DeviceMode.IDLE.getLabel());
                        states.add(DeviceMode.COOK_RICE.getLabel());
                        states.add(DeviceMode.STEAM_COOK.getLabel());
                        states.add(DeviceMode.PORRIDGE.getLabel());
                        states.add(DeviceMode.CAKE.getLabel());
                        break;
                    case "STERILIZER":
                        states.add(DeviceMode.IDLE.getLabel());
                        states.add(DeviceMode.HIGH_TEMP.getLabel());
                        states.add(DeviceMode.UVB.getLabel());
                        states.add(DeviceMode.STERILIZER_DRY.getLabel());
                        break;
                    case "MICROWAVE":
                        states.add(DeviceMode.IDLE.getLabel());
                        states.add(DeviceMode.HEAT.getLabel());
                        states.add(DeviceMode.GRILL.getLabel());
                        states.add(DeviceMode.DEFROST.getLabel());
                        states.add(DeviceMode.STEAM.getLabel());
                        break;
                }
            }
            condStateList.postValue(states);
        });
    }

    /**
     * 当作为 [执行动作] 的设备被选中时，加载它的指令列表
     */
    public void performTheAction(String deviceName) {
        ThreadPoolUtils.getInstance().execute(() -> {
            Device device = findDeviceByNameSync(deviceName);
            if (device == null) return;
            List<String> cmds = new ArrayList<>();
            cmds.add(DeviceStatus.ONLINE.getState());
            cmds.add(DeviceStatus.OFFLINE.getState());
            if (device.getDeviceType() != null) {
                switch (device.getDeviceType()) {
                    case "REFRIGERATOR":
                        cmds.add(DeviceMode.STANDARD.getLabel());
                        cmds.add(DeviceMode.FAST_COOL.getLabel());
                        cmds.add(DeviceMode.ENERGY_SAVING.getLabel());
                        cmds.add(DeviceMode.HOLIDAY.getLabel());
                        break;
                    case "DISHWASHER":
                        cmds.add(DeviceMode.IDLE.getLabel());
                        cmds.add(DeviceMode.STANDARD_WASH.getLabel());
                        cmds.add(DeviceMode.QUICK_WASH.getLabel());
                        cmds.add(DeviceMode.INTENSIVE_WASH.getLabel());
                        cmds.add(DeviceMode.ECO_WASH.getLabel());
                        cmds.add(DeviceMode.SANITIZE_WASH.getLabel());
                        cmds.add(DeviceMode.DRY.getLabel());
                    case "RICE_COOKER":
                        cmds.add(DeviceMode.IDLE.getLabel());
                        cmds.add(DeviceMode.COOK_RICE.getLabel());
                        cmds.add(DeviceMode.STEAM_COOK.getLabel());
                        cmds.add(DeviceMode.PORRIDGE.getLabel());
                        cmds.add(DeviceMode.CAKE.getLabel());
                        break;
                    case "STERILIZER":
                        cmds.add(DeviceMode.IDLE.getLabel());
                        cmds.add(DeviceMode.HIGH_TEMP.getLabel());
                        cmds.add(DeviceMode.UVB.getLabel());
                        cmds.add(DeviceMode.STERILIZER_DRY.getLabel());
                        break;
                    case "MICROWAVE":
                        cmds.add(DeviceMode.IDLE.getLabel());
                        cmds.add(DeviceMode.HEAT.getLabel());
                        cmds.add(DeviceMode.GRILL.getLabel());
                        cmds.add(DeviceMode.DEFROST.getLabel());
                        cmds.add(DeviceMode.STEAM.getLabel());
                        break;
                }
            }
            actionCommandList.postValue(cmds);
        });
    }

    public void deleteRule(String ruleId) {
        // 由于 Repository 已做了乐观更新，此处直接调即可
        repository.deleteRule(ruleId, new SmartRepository.SmartCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "删除规则失败回退: " + errorMessage);
            }
        });
    }

    public void executePresetScenarios(int scenarioId) {
        String ruleMode = "";
        switch (scenarioId) {
            case 0:
                ruleMode = "SCENE_MEALPREP";
                break;
            case 1:
                ruleMode = "SCENE_LEAVE_HOME";
                break;
            case 2:
                ruleMode = "SCENE_CLEANUP";
                break;
        }
        repository.executeScene(ruleMode, new SmartRepository.SmartCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

    }

    /**
     * 提交自定义规则，组装成 DTO 结构，并通过 Repository 保存到后端数据库
     */
    public void submitRule() {
        // 【修复】放置在子线程避免查询数据库阻塞主线程
        ThreadPoolUtils.getInstance().execute(() -> {
            int type = selectedConditionType.getValue() != null ? selectedConditionType.getValue() : 1;
            RuleCondition condition = null;

            if (type == 0) {
                condition = new RuleCondition("TIME", "cron", "==", selectedTime.getValue());
            } else if (type == 1) {
                condition = new RuleCondition("SENSOR", "value", ">=", String.valueOf(sensorThreshold.getValue()));
            } else {
                String condDeviceName = selectedCondDeviceName.getValue();
                String condStateName = selectedCondStateName.getValue();
                if (condDeviceName == null || condStateName == null) return;

                Device condDevice = findDeviceByNameSync(condDeviceName);
                if (condDevice == null) return;

                // 【修复】解除 "SK-R001" 硬编码，修复映射逻辑
                if (!DeviceStatus.fromState(condStateName).equals(DeviceStatus.UNKNOWN.name())) {
                    condition = new RuleCondition("STATUS", condDevice.getDeviceSn(), "status", "==", DeviceStatus.fromState(condStateName));
                } else {
                    condition = new RuleCondition("STATUS", condDevice.getDeviceSn(), "mode", "==", DeviceMode.toMode(condStateName));
                }
            }

            String actionDeviceName = selectedActionDeviceName.getValue();
            String actionCommandName = selectedActionCommandName.getValue();
            if (actionDeviceName == null || actionCommandName == null) return;

            Device actionDevice = findDeviceByNameSync(actionDeviceName);
            if (actionDevice == null) return;

            RuleAction action;
            // 【修复】映射逻辑颠倒：如从文字解析出状态，则 property 应该是 status；否则是 mode
            if (!DeviceStatus.fromState(actionCommandName).equals(DeviceStatus.UNKNOWN.name())) {
                action = new RuleAction("DEVICE_CONTROL", actionDevice.getDeviceSn(), "status", DeviceStatus.fromState(actionCommandName));
            } else {
                action = new RuleAction("DEVICE_CONTROL", actionDevice.getDeviceSn(), "mode", DeviceMode.toMode(actionCommandName));
            }

            AutomationRuleDTO ruleDTO = new AutomationRuleDTO(ruleName.getValue(), true, condition, action);

            repository.createRule(ruleDTO, new SmartRepository.SmartCallback() {
                @Override
                public void onSuccess() {
                    syncRulesList();
                    saveRuleResult.postValue(true);
                }

                @Override
                public void onError(String errorMessage) {
                    syncRulesList();
                    saveRuleResult.postValue(true);
                }
            });
        });
    }

    // Getter methods
    public LiveData<List<String>> getSensorList() {
        return sensorList;
    }

    public LiveData<List<String>> getDeviceList() {
        ThreadPoolUtils.getInstance().execute(() -> {
            List<String> devices = new ArrayList<>();
            List<Device> repDevices = repository.getDeviceList();

            if (repDevices != null) {
                for (Device device : repDevices) {
                    devices.add(device.getDeviceName());
                }
            }
            deviceList.postValue(devices);
        });
        return deviceList;
    }

    public LiveData<List<String>> getCondStateList() {
        return condStateList;
    }

    public LiveData<List<String>> getActionCommandList() {
        return actionCommandList;
    }

    public LiveData<List<Rules>> getRulesList() {
        rulesList = repository.getRulesList();
        return rulesList;
    }

}