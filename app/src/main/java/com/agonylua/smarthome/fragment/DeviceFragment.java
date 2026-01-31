package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.databinding.FragmentDeviceBinding;
import com.agonylua.smarthome.databinding.LayoutMicrowaveBinding;
import com.agonylua.smarthome.databinding.LayoutRefrigeratorBinding;
import com.agonylua.smarthome.repository.DeviceRepository;
import com.agonylua.smarthome.viewModel.DeviceViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

public class DeviceFragment extends Fragment {

    // 常量定义
    public static final String TYPE_FRIDGE = "fridge";
    public static final String TYPE_MICROWAVE = "microwave";
    private static final String SUB_TOPIC = "smartKitchen/App/";
    private static final String TAG = "DeviceFragment";
    private FragmentDeviceBinding binding;
    private DeviceViewModel mViewModel;
    private DeviceRepository deviceRepository;
    private Device device;
    private String mDeviceType, mDeviceName, mDeviceState;

    // 无参构造
    public DeviceFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        binding.setViewModel(mViewModel);
        if (getArguments() != null) {
            DeviceFragmentArgs args = DeviceFragmentArgs.fromBundle(getArguments());
            device = args.getCurrentDevice();
            this.mDeviceType = device.getDeviceType();
            this.mDeviceName = device.getDeviceName();
            this.mDeviceState = device.getDeviceStatus();
        }
        if (getActivity() != null) {
            binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        }
        setupDynamicContent(mDeviceType);
        observeViewModel();
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        mViewModel.autoSaverSetup.observe(getViewLifecycleOwner(), trigger -> {
            mViewModel.saveDataGeneral();
        });
        mViewModel.getDevice(device.getDeviceSn()).observe(getViewLifecycleOwner(), updatedDevice -> {
            if (updatedDevice != null) {
                this.device = updatedDevice;
                mViewModel.initDevice(this.device);
            }
        });
    }


    private void setupDynamicContent(String deviceType) {
        FrameLayout container = binding.containerDevice;
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View contentView;

        switch (deviceType) {
            case "REFRIGERATOR": // 冰箱
                LayoutRefrigeratorBinding fridgeBinding = LayoutRefrigeratorBinding.inflate(inflater, container, false);
                fridgeBinding.setViewModel(mViewModel);
                fridgeBinding.setDevice(device);
                mViewModel.initDevice(device);
                fridgeBinding.btnStart.setOnClickListener(v -> {
                    mViewModel.submitTask();
                });
                fridgeBinding.setLifecycleOwner(getViewLifecycleOwner());

                container.addView(fridgeBinding.getRoot());
                break;

            case "MICROWAVE": // 微波炉
                binding.toolbar.setTitle("微波炉");
                binding.deviceImage.setImageResource(R.drawable.ic_device_microwave_online);
                LayoutMicrowaveBinding microwaveBinding = LayoutMicrowaveBinding.inflate(inflater, container, false);
                microwaveBinding.setViewModel(mViewModel);
                microwaveBinding.setLifecycleOwner(getViewLifecycleOwner());
                container.addView(microwaveBinding.getRoot());
                initMicrowaveLogic(microwaveBinding.getRoot());
                break;
            case "DISHWASHER": // 洗碗机
                binding.toolbar.setTitle("洗碗机");
                binding.deviceImage.setImageResource(R.drawable.ic_device_dishwasher_online);
                break;
            case "RICE_COOKER": // 电饭煲
                binding.toolbar.setTitle("智能电饭煲");
                binding.deviceImage.setImageResource(R.drawable.ic_device_rice_cooker_online);
                contentView = inflater.inflate(R.layout.layout_rice_cooker, container, false);
                container.addView(contentView);
                break;
            case "STERILIZER": // 消毒柜
                binding.toolbar.setTitle("智能消毒柜");
                binding.deviceImage.setImageResource(R.drawable.ic_device_sterilizer_online);
                contentView = inflater.inflate(R.layout.layout_sterilizer, container, false);
                container.addView(contentView);
                break;
        }
    }

    // ============================================================
    //  设备逻辑：冰箱 (Fridge)
    //  假设布局包含: Slider(温度), ChipGroup(模式: 速冻/节能)
    // ============================================================

    // ============================================================
    //  设备逻辑：微波炉 (Microwave)
    //  假设布局包含: ChipGroup(时间选择), ChipGroup(火力), Button(开始)
    // ============================================================
    private void initMicrowaveLogic(View view) {
        TextView tvTimer = view.findViewById(R.id.tv_timer_big); // 巨大的数字显示
        Slider sliderTime = view.findViewById(R.id.slider_time); // 预设时间: 30s, 1min, 3min
        ChipGroup chipGroupPower = view.findViewById(R.id.chip_group_power); // 火力: 高火, 解冻
        MaterialButton btnStart = view.findViewById(R.id.btn_start_microwave);

        // 保存当前选择的时间 (在 Fragment 暂存 UI 状态，或者放到 ViewModel 也可以)
        final int[] currentSeconds = {0};

        // 2. 事件绑定
        if (chipGroupPower != null) {
            chipGroupPower.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;

                // 简单的根据 ID 或 Tag 判断时间，这里简化处理
                View chip = view.findViewById(checkedIds.get(0));
                int seconds = 0;

                // 假设 XML 中 chip 的 tag 存了秒数，或者根据 Text 判断
                String text = ((Chip) chip).getText().toString();
                if (text.contains("30秒")) seconds = 30;
                else if (text.contains("1分钟")) seconds = 60;
                else if (text.contains("3分钟")) seconds = 180;

                currentSeconds[0] = seconds;
                mViewModel.setMicrowaveTime(seconds); // 更新 UI 显示
            });
        }
        if (sliderTime != null) {
            // 滑动停止后才发送请求
            sliderTime.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                }
            });

            // 滑动时仅做本地 UI 反馈 (可选，也可以通过 LiveData)
            sliderTime.addOnChangeListener((slider, value, fromUser) -> {
                sliderTime.setValue(value);
            });
        }
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                String power = "高火"; // 默认为高火，或者从 chipGroupPower 获取
                if (chipGroupPower != null && chipGroupPower.getCheckedChipId() != View.NO_ID) {
                    Chip c = view.findViewById(chipGroupPower.getCheckedChipId());
                    power = c.getText().toString();
                }
                mViewModel.startMicrowave(currentSeconds[0], power);
            });
        }

    }

    // ====================== 通用 ========================


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mViewModel.saveDataGeneral(mDeviceType, mDeviceName);
        binding = null;
    }
}