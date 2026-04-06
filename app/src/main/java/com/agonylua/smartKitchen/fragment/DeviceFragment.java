package com.agonylua.smartKitchen.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.databinding.FragmentDeviceBinding;
import com.agonylua.smartKitchen.databinding.LayoutDishwasherBinding;
import com.agonylua.smartKitchen.databinding.LayoutMicrowaveBinding;
import com.agonylua.smartKitchen.databinding.LayoutRefrigeratorBinding;
import com.agonylua.smartKitchen.databinding.LayoutRiceCookerBinding;
import com.agonylua.smartKitchen.databinding.LayoutSterilizerBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.DeviceViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeviceFragment extends Fragment {

    // 常量定义
    public static final String TYPE_FRIDGE = "fridge";
    public static final String TYPE_MICROWAVE = "microwave";
    private static final String SUB_TOPIC = "smartKitchen/App/";
    private static final String TAG = "DeviceFragment";
    private FragmentDeviceBinding binding;
    private DeviceViewModel mViewModel;
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
            binding.deviceSn.setText(device.getDeviceSn());
        }
        if (getActivity() != null) {
            binding.toolbar.setNavigationOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack();
            });
        }

        binding.deviceStatus.setOnClickListener(v -> {
            mViewModel.statusLoading.setValue(true);
            mViewModel.updateDeviceStatus(device.getDeviceSn());
        });
        setupDynamicContent(mDeviceType);
        observeViewModel();
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        mViewModel.autoSaverSetup.observe(getViewLifecycleOwner(), trigger -> {
            mViewModel.saveDataGeneral();
        });
        mViewModel.getDevice().observe(getViewLifecycleOwner(), updatedDevice -> {
            mViewModel.subTaskLoading.setValue(false);
            if (updatedDevice != null) {
                this.device = updatedDevice;
                mViewModel.initDevice(this.device);
            }
        });
        mViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                SnackbarUtils.show(requireView(), message);
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
                LayoutMicrowaveBinding microwaveBinding = LayoutMicrowaveBinding.inflate(inflater, container, false);
                microwaveBinding.setViewModel(mViewModel);
                microwaveBinding.setDevice(device);
                Log.d(TAG, "setupDynamicContent: " + device.getDeviceMode());
                mViewModel.initDevice(device);

                // 5. 开始加热按钮
                microwaveBinding.btnStart.setOnClickListener(v -> {
                    mViewModel.submitTask();
                });

                microwaveBinding.setLifecycleOwner(getViewLifecycleOwner());
                container.addView(microwaveBinding.getRoot());
                break;
            case "DISHWASHER": // 洗碗机
                LayoutDishwasherBinding dishwasherBinding = LayoutDishwasherBinding.inflate(inflater, container, false);
                dishwasherBinding.setViewModel(mViewModel);
                mViewModel.initDevice(device);
                dishwasherBinding.btnStart.setOnClickListener(v -> {
                    mViewModel.submitTask();
                });

                dishwasherBinding.setLifecycleOwner(getViewLifecycleOwner());
                container.addView(dishwasherBinding.getRoot());
                break;
            case "RICE_COOKER": // 电饭煲
                LayoutRiceCookerBinding riceCookerBinding = LayoutRiceCookerBinding.inflate(inflater, container, false);
                riceCookerBinding.setViewModel(mViewModel);
                riceCookerBinding.setDevice(device);
                mViewModel.initDevice(device);
                riceCookerBinding.btnStart.setOnClickListener(v -> {
                    mViewModel.submitTask();
                });

                riceCookerBinding.setLifecycleOwner(getViewLifecycleOwner());
                container.addView(riceCookerBinding.getRoot());
                break;
            case "STERILIZER": // 消毒柜
                LayoutSterilizerBinding sterilizerBinding = LayoutSterilizerBinding.inflate(inflater, container, false);
                sterilizerBinding.setViewModel(mViewModel);
                mViewModel.initDevice(device);
                sterilizerBinding.btnStart.setOnClickListener(v -> {
                    mViewModel.submitTask();
                });

                sterilizerBinding.setLifecycleOwner(getViewLifecycleOwner());
                container.addView(sterilizerBinding.getRoot());
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}