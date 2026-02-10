package com.agonylua.smarthome.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentProvisionBinding;
import com.agonylua.smarthome.viewModel.ProvisionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProvisionFragment extends Fragment {

    private FragmentProvisionBinding binding;
    private ProvisionViewModel viewModel;

    // 权限与蓝牙开启的 Launcher
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册权限回调
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );

        // 注册蓝牙开启回调
        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        //autoStartScan();
                    } else {
                        Toast.makeText(requireContext(), "必须开启蓝牙才能连接设备", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProvisionBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProvisionViewModel.class);
        binding.setViewModel(viewModel);
        observeViewModel();

        initListeners();

        // 进入页面立即检查权限并尝试连接
        checkPermissionsAndAutoConnect();
    }

    private void initListeners() {
        binding.toolbar.setNavigationOnClickListener(view -> {
            if (isAdded() && getView() != null) {
                try {
                    Navigation.findNavController(view).navigateUp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.btnStartProvision.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsConnected().getValue())) {
                String ssid = binding.etSsid.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(ssid)) {
                    binding.etSsid.setError("请输入 WiFi 名称");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    binding.etPassword.setError("请输入 WiFi 密码");
                    return;
                }
                // 隐藏软键盘
                hideKeyboard();
                // 开始发送 WiFi 配置
                viewModel.startProvisioning(ssid, password);
            } else {
                Navigation.findNavController(v).navigate(R.id.scanQrFragment);
            }
        });
    }

    private void observeViewModel() {
        // 监听二维码解析结果
        viewModel.getQrCodeParsed().observe(getViewLifecycleOwner(), parsed -> {
            if (parsed) {
                viewModel.startBleScan();
            }
        });
        // 监听配网结果
        viewModel.getProvisionStatus().observe(getViewLifecycleOwner(), status -> {
            if ("Success".equals(status)) {
                Toast.makeText(getContext(), "设备配网成功！", Toast.LENGTH_LONG).show();

                // 成功后延迟 1秒 退出或跳转
                binding.getRoot().postDelayed(() -> {
                    if (isAdded() && getView() != null) {
                        try {
                            //viewModel.reset();
                            Navigation.findNavController(binding.getRoot()).navigateUp();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
            }
        });
    }

    /**
     * 自动流程第一步：检查权限
     */
    private void checkPermissionsAndAutoConnect() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // Android 11 及以下
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            // 申请权限
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            // 权限已有，检查蓝牙开关
            checkBluetoothEnabled();
        }
    }

    private void handlePermissionResult(Map<String, Boolean> result) {
        boolean allGranted = true;
        for (Boolean granted : result.values()) {
            if (!granted) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            checkBluetoothEnabled();
        } else {
            showPermissionSettingsDialog();
        }
    }

    private void checkBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            // 请求开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
        } else {
            // 一切就绪，开始扫描
            //autoStartScan();
        }
    }

    /**
     * 自动流程最后一步：通知 ViewModel 开始扫描
     */
    private void autoStartScan() {
        viewModel.startBleScan();
    }

    private void showPermissionSettingsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("权限不足")
                .setMessage("连接智能设备需要蓝牙和定位权限，请前往设置开启。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("退出", (dialog, which) -> Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment))
                .setCancelable(false)
                .show();
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.reset();
    }
}