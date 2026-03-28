package com.agonylua.smarthome.viewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smarthome.dto.AutomationRuleDTO;
import com.agonylua.smarthome.dto.RuleAction;
import com.agonylua.smarthome.dto.RuleCondition;
import com.agonylua.smarthome.repository.SmartRepository;

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
    public final MutableLiveData<String> selectedSensorName = new MutableLiveData<>("厨房温湿度传感器");
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
    private SmartRepository repository;

    public SmartViewModel() {
        loadMockData();
        repository = new SmartRepository();
    }

    private void loadMockData() {
        // 传感器列表
        List<String> sensors = new ArrayList<>();
        sensors.add("厨房温湿度传感器(温度)");
        sensors.add("厨房温湿度传感器(湿度)");
        sensors.add("电子秤(重量)");
        sensorList.setValue(sensors);

        // 家电列表 (既可以作为状态触发源，也可以作为执行目标)
        List<String> appliances = new ArrayList<>();
        appliances.add("智能冰箱");
        appliances.add("智能洗碗机");
        appliances.add("智能消毒柜");
        appliances.add("智能电饭煲");
        appliances.add("智能微波炉");
        deviceList.setValue(appliances);
    }

    /**
     * 当作为 [触发条件] 的设备被选中时，加载它的状态列表
     */
    public void updateStatesForConditionDevice(String deviceName) {
        List<String> states = new ArrayList<>();
        if (deviceName != null && deviceName.contains("冰箱")) {
            states.add("门被打开持续3分钟");
            states.add("冷藏室温度过高");
        } else if (deviceName != null && (deviceName.contains("洗碗机") || deviceName.contains("电饭煲"))) {
            states.add("工作完成 (FINISHED)");
            states.add("开始运行 (RUNNING)");
        } else {
            states.add("状态改变");
        }
        condStateList.setValue(states);
        if (!states.isEmpty()) selectedCondStateName.setValue(states.get(0));
    }

    /**
     * 当作为 [执行动作] 的设备被选中时，加载它的指令列表
     */
    public void updateCommandsForActionDevice(String deviceName) {
        List<String> cmds = new ArrayList<>();
        if (deviceName != null && deviceName.contains("消毒柜")) {
            cmds.add("开启自动防霉烘干");
            cmds.add("开启紫外线杀菌");
        } else if (deviceName != null && deviceName.contains("微波炉")) {
            cmds.add("开启智能解冻");
        } else if (deviceName != null && deviceName.contains("冰箱")) {
            cmds.add("开启速冻模式");
            cmds.add("开启节能模式");
        } else {
            cmds.add("开启");
            cmds.add("关闭");
        }
        actionCommandList.setValue(cmds);
        if (!cmds.isEmpty()) selectedActionCommandName.setValue(cmds.get(0));
    }

    // Getter methods
    public LiveData<List<String>> getSensorList() {
        return sensorList;
    }

    public LiveData<List<String>> getDeviceList() {
        return deviceList;
    }

    public LiveData<List<String>> getCondStateList() {
        return condStateList;
    }

    public LiveData<List<String>> getActionCommandList() {
        return actionCommandList;
    }

    /**
     * 组装最终提交的 JSON
     */
    public void submitRule(Context context) {
        RuleCondition condition;
        int type = selectedConditionType.getValue() != null ? selectedConditionType.getValue() : 1;

        // 1. 根据用户选择的类型，动态生成不同的 Condition 结构
        if (type == 0) { // 时间触发
            condition = new RuleCondition("TIME", null, "cron", "==", selectedTime.getValue());
        } else if (type == 1) { // 传感器触发
            condition = new RuleCondition("SENSOR", selectedSensorName.getValue(), "value", ">", String.valueOf(sensorThreshold.getValue()));
        } else { // 设备状态触发
            condition = new RuleCondition("DEVICE_STATE", selectedCondDeviceName.getValue(), "status", "==", selectedCondStateName.getValue());
        }

        // 2. 组装 Action
        RuleAction action = new RuleAction("DEVICE_CONTROL", selectedActionDeviceName.getValue(), selectedActionCommandName.getValue(), "");

        // 3. 组装完整 DTO
        AutomationRuleDTO ruleDTO = new AutomationRuleDTO(ruleName.getValue(), true, condition, action);

        // TODO: 利用 Retrofit 发起网络请求保存至 Spring Boot MySQL
        repository.saveRule(context, ruleDTO, new SmartRepository.SmartCallback() {
            @Override
            public void onSuccess() {
                saveRuleResult.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                saveRuleResult.postValue(false);
            }
        });
    }
}