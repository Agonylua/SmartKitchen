package com.agonylua.smartKitchen.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.FragmentProvisionBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.ProvisionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProvisionFragment extends Fragment {

    private FragmentProvisionBinding binding;
    private ProvisionViewModel viewModel;

    // 权限与硬件开启的 Launchers
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;
    private ActivityResultLauncher<Intent> enableLocationLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册权限申请回调
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );

        // 注册蓝牙开启回调
        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        checkHardwareSwitches(); // 蓝牙开了，继续查定位
                    } else {
                        SnackbarUtils.show(requireView(), "必须开启蓝牙才能连接设备");
                    }
                }
        );

        // 注册系统定位开关开启回调
        enableLocationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    checkHardwareSwitches(); // 从设置页面回来后重新检查
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
    }

    private void initListeners() {
        binding.ivBack.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.action_provision_to_main);
        });

        binding.btnRescan.setOnClickListener(v -> {
            viewModel.resetState();
            NavController navController = Navigation.findNavController(getView());
            navController.popBackStack();
        });

        binding.btnStartProvision.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsConnected().getValue())) {
                String ssid = binding.etSsid.getText() != null ? binding.etSsid.getText().toString().trim() : "";
                String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

                if (TextUtils.isEmpty(ssid)) {
                    binding.etSsid.setError("请输入 WiFi 名称");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    binding.etPassword.setError("请输入 WiFi 密码");
                    return;
                }
                binding.etSsid.setError(null);
                binding.etPassword.setError(null);

                hideKeyboard();
                viewModel.startProvisioning(ssid, password);
            } else {
                SnackbarUtils.show(requireView(), "请等待蓝牙连接成功后再发送 WiFi");
            }
        });
    }

    private void observeViewModel() {
        // 监听设备绑定成果信息并 Toast 提示
        viewModel.getBindResultMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                SnackbarUtils.show(requireView(), msg);
                viewModel.clearBindResultMsg();
            }
        });

        // 只有当验证且绑定成功后，此处变为 true，触发严格的权限检查链
        viewModel.getQrCodeParsed().observe(getViewLifecycleOwner(), parsed -> {
            if (parsed != null && parsed) {
                // 确保没有发生错误
                if (Boolean.FALSE.equals(viewModel.getIsScanError().getValue())) {
                    checkPermissionsAndAutoConnect();
                }
            }
        });

        // 状态提示
        viewModel.getScanStatus().observe(getViewLifecycleOwner(), statusMsg -> {
            if (statusMsg != null && binding.tvStatusMessage != null) {
                binding.tvStatusMessage.setText(statusMsg);
                binding.llLoadingStatus.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getProvisionStatus().observe(getViewLifecycleOwner(), status -> {
            if ("Success".equals(status)) {
                SnackbarUtils.show(requireView(), "设备配网成功！");
                binding.getRoot().postDelayed(() -> {
                    NavController navController = Navigation.findNavController(getView());
                    navController.navigate(R.id.action_provision_to_main);
                }, 2000); // 2秒后返回上一页
            } else if (status != null && (status.contains("失败") || status.contains("异常"))) {
                SnackbarUtils.show(requireView(), "设备配网失败，请重试");
            }
            if (status != null && binding.tvStatusMessage != null) {
                binding.tvStatusMessage.setText(status);
            }
        });

        viewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected != null && isConnected) {
                binding.btnStartProvision.setEnabled(true);
                binding.btnStartProvision.setText("发送 Wi-Fi 配置");
                binding.llLoadingStatus.setVisibility(View.GONE);
            } else {
                binding.btnStartProvision.setEnabled(false);
                binding.btnStartProvision.setText("等待蓝牙连接...");
            }
        });
    }

    /**
     * 权限拦截第一关：应用层权限
     */
    private void checkPermissionsAndAutoConnect() {
        List<String> permissionsNeeded = new ArrayList<>();

        // 扫描和连接权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        // 定位权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            // 权限都有了，检查硬件开关
            checkHardwareSwitches();
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
            checkHardwareSwitches();
        } else {
            showPermissionSettingsDialog("配网需要获取附近的蓝牙设备，请前往系统设置授予位置和蓝牙权限。");
        }
    }

    /**
     * 权限拦截第二关：手机硬件开关 (蓝牙与GPS)
     */
    private void checkHardwareSwitches() {
        // 查蓝牙
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
            return;
        }

        // 查系统定位
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationServiceDialog();
            return;
        }

        // 过了所有关卡，发起真正的扫描！
        autoStartScan();
    }

    private void autoStartScan() {
        if (Boolean.TRUE.equals(viewModel.getQrCodeParsed().getValue()) &&
                Boolean.FALSE.equals(viewModel.getIsScanError().getValue())) {
            viewModel.startBleScan();
        }
    }

    private void showPermissionSettingsDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("权限不足")
                .setMessage(message)
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("退出", (dialog, which) -> {
                    NavController navController = Navigation.findNavController(getView());
                    navController.navigate(R.id.action_provision_to_main);
                })
                .setCancelable(false)
                .show();
    }

    private void showLocationServiceDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("定位服务未开启")
                .setMessage("Android 系统要求进行蓝牙扫描时必须开启系统定位服务（GPS），请开启后再试。")
                .setPositiveButton("去开启", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    enableLocationLauncher.launch(intent);
                })
                .setNegativeButton("退出", (dialog, which) -> {
                    NavController navController = Navigation.findNavController(getView());
                    navController.navigate(R.id.action_provision_to_main);
                })
                .setCancelable(false)
                .show();
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.resetState();
        }
    }
}