package com.agonylua.smartKitchen.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.DialogAddRuleBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.SmartViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CustomRulesFragment extends BottomSheetDialogFragment {
    private static final String TAG = "CustomRulesFragment";
    private DialogAddRuleBinding binding;
    private SmartViewModel smartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddRuleBinding.inflate(inflater, container, false);
        smartViewModel = new ViewModelProvider(requireActivity()).get(SmartViewModel.class);
        binding.setViewModel(smartViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupConditionTypeToggle();
        setupDropdownMenus();
        setupTimePicker();
        observeSaveResult();
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    /**
     * 监听顶部三大触发类型的切换 (时间 / 传感器 / 设备状态)
     */
    private void setupConditionTypeToggle() {
        // 初始化选中状态
        binding.toggleConditionType.check(R.id.btnTypeSensor);

        binding.toggleConditionType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            smartViewModel.selectedCondDeviceName.setValue("");
            smartViewModel.selectedCondStateName.setValue("");
            smartViewModel.selectedActionDeviceName.setValue("");
            smartViewModel.selectedActionCommandName.setValue("");
            if (isChecked) {
                if (checkedId == R.id.btnTypeTime) {
                    smartViewModel.selectedConditionType.setValue(0);
                } else if (checkedId == R.id.btnTypeSensor) {
                    smartViewModel.selectedConditionType.setValue(1);
                } else if (checkedId == R.id.btnTypeDevice) {
                    smartViewModel.selectedConditionType.setValue(2);
                }
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            Log.d(TAG, "setupConditionTypeToggle: 保存按钮被点击，准备提交规则");
            smartViewModel.submitRule();
        });
    }

    /**
     * 时间选择器 (Material 3)
     */
    private void setupTimePicker() {
        binding.btnPickTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(8)
                    .setMinute(0)
                    .setTitleText("选择定时触发时间")
                    .build();

            picker.addOnPositiveButtonClickListener(dialog -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
                smartViewModel.selectedTime.setValue(time);
            });

            picker.show(getChildFragmentManager(), "RuleTimePicker");
        });
    }

    /**
     * 设置所有的下拉列表联动逻辑
     */
    private void setupDropdownMenus() {
        // [IF] 1. 传感器下拉列表
        smartViewModel.getSensorList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                binding.actvSensor.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, list));
        });

        // [IF] 2. 设备状态触发源下拉列表
        smartViewModel.getDeviceList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                binding.actvCondDevice.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, list));
        });

        // [IF] 3. 监听触发源设备选择 -> 刷新它的状态列表
        binding.actvCondDevice.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            binding.actvCondState.setEnabled(selectedDevice != null);
            smartViewModel.updateStatesForConditionDevice(selectedDevice);
        });

        // [IF] 4. 触发源设备具体状态列表
        smartViewModel.getCondStateList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                binding.actvCondState.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, list));
        });

        // ==========================================

        // [THEN] 1. 执行目标设备下拉列表
        smartViewModel.getDeviceList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                binding.actvActionDevice.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, list));
        });

        // [THEN] 2. 监听执行设备选择 -> 刷新它的指令列表
        binding.actvActionDevice.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            binding.actvActionCommand.setEnabled(selectedDevice != null);
            smartViewModel.performTheAction(selectedDevice);
        });

        // [THEN] 3. 执行指令列表
        smartViewModel.getActionCommandList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                binding.actvActionCommand
                        .setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, list));
        });
    }

    private void observeSaveResult() {
        smartViewModel.saveRuleResult.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    SnackbarUtils.show(requireView(), "自动化规则创建成功！");
                    dismiss();
                } else {
                    SnackbarUtils.show(requireView(), "规则保存失败，请重试");
                }
                smartViewModel.saveRuleResult.setValue(null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}