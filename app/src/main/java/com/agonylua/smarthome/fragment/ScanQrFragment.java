package com.agonylua.smarthome.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.viewModel.ProvisionViewModel;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class ScanQrFragment extends Fragment {

    private CodeScanner mCodeScanner;
    private ProvisionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 注意：这里使用 requireActivity() 获取 ViewModel，
        // 这样 ScanQrFragment 和 ProvisionFragment 可以共享同一个 ViewModel 实例，数据不丢失
        viewModel = new ViewModelProvider(requireActivity()).get(ProvisionViewModel.class);
        observeViewModel();

        setupScanner(view);
        checkCameraPermission();
    }

    private void observeViewModel() {
        // 监听二维码解析结果
        viewModel.getQrCodeParsed().observe(getViewLifecycleOwner(), parsed -> {
            if (parsed) {
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.provisionFragment);
                }
            }
        });
    }

    private void setupScanner(View view) {
        mCodeScanner = new CodeScanner(requireContext(), view.findViewById(R.id.scanner_view));

        // 设置扫描回调
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                requireActivity().runOnUiThread(() -> {
                    // 1. 获取到二维码数据
                    String qrData = result.getText();

                    // 2. 交给 ViewModel 解析
                    viewModel.parseQrCode(qrData);

                    // 3. 观察解析结果，如果成功则跳转到输入 WiFi 密码的界面
                    Navigation.findNavController(view).navigateUp();
                });
            }
        });

        // 点击重新扫描
        view.findViewById(R.id.scanner_view).setOnClickListener(v -> mCodeScanner.startPreview());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            mCodeScanner.startPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCodeScanner.startPreview();
        } else {
            Toast.makeText(requireContext(), "需要相机权限进行扫码", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    public void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }
}