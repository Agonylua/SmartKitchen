package com.agonylua.smarthome.fragment;

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

    // 权限与硬件开启的 Launchers
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;
    private ActivityResultLauncher<Intent> enableLocationLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 注册权限申请回调
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );

        // 2. 注册蓝牙开启回调
        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        checkHardwareSwitches(); // 蓝牙开了，继续查定位
                    } else {
                        Toast.makeText(requireContext(), "必须开启蓝牙才能连接设备", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 3. 注册系统定位开关开启回调
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
        binding.ivBack.setOnClickListener(view -> Navigation.findNavController(getView()).popBackStack(R.id.mainFragment, false));

        binding.btnRescan.setOnClickListener(v -> {
            viewModel.resetState();
            Navigation.findNavController(getView()).navigate(R.id.scanQrFragment);
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
                Toast.makeText(requireContext(), "请等待蓝牙连接成功后再发送 WiFi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        // 1. 监听设备绑定成果信息并 Toast 提示
        viewModel.getBindResultMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                viewModel.clearBindResultMsg();
            }
        });

        // 2. 只有当验证且绑定成功后，此处变为 true，触发严格的权限检查链
        viewModel.getQrCodeParsed().observe(getViewLifecycleOwner(), parsed -> {
            if (parsed != null && parsed) {
                // 确保没有发生错误
                if (Boolean.FALSE.equals(viewModel.getIsScanError().getValue())) {
                    checkPermissionsAndAutoConnect();
                }
            }
        });

        // 3. 状态提示
        viewModel.getScanStatus().observe(getViewLifecycleOwner(), statusMsg -> {
            if (statusMsg != null && binding.tvStatusMessage != null) {
                binding.tvStatusMessage.setText(statusMsg);
                binding.llLoadingStatus.setVisibility(View.VISIBLE);
            }
        });

        // 4. 配网结果
        viewModel.getProvisionStatus().observe(getViewLifecycleOwner(), status -> {
            if ("Success".equals(status)) {
                Toast.makeText(getContext(), "设备配网成功！", Toast.LENGTH_LONG).show();
                binding.getRoot().postDelayed(() -> Navigation.findNavController(getView()).popBackStack(R.id.mainFragment, false), 2000); // 2秒后返回主页
            } else if (status != null && (status.contains("失败") || status.contains("异常"))) {
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
            }
            if (status != null && binding.tvStatusMessage != null) {
                binding.tvStatusMessage.setText(status);
            }
        });

        // 5. 蓝牙连接成功状态
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

        // Android 12+ (S) 需要扫描和连接权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        // 任何版本都需要精确定位权限才能扫描到 BLE 广播包
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
        // 1. 查蓝牙
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
            return;
        }

        // 2. 查系统定位(GPS)开关 —— 这是很多国产安卓机静默拦截 BLE 扫描的罪魁祸首！
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
        // 双重校验：确保二维码没出错且已经验证放行才启动
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
                .setNegativeButton("退出", (dialog, which) -> Navigation.findNavController(getView()).popBackStack(R.id.mainFragment, false))
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
                .setNegativeButton("退出", (dialog, which) -> Navigation.findNavController(getView()).popBackStack(R.id.mainFragment, false))
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