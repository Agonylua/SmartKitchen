package com.agonylua.smartKitchen.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.utils.PermissionUtils;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.ProvisionViewModel;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanQrFragment extends Fragment {

    private CodeScanner mCodeScanner;
    private ProvisionViewModel viewModel;

    // 使用我们之前生成的权限工具类处理权限回调
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (mCodeScanner != null) mCodeScanner.startPreview();
                } else {
                    SnackbarUtils.show(requireView(), "需要相机权限进行扫码");
                }
            });

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 共享 Activity 级别的 ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ProvisionViewModel.class);

        setupScanner(view);
        checkCameraPermission();
    }

    private void setupScanner(View view) {
        mCodeScanner = new CodeScanner(requireContext(), view.findViewById(R.id.scanner_view));

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                requireActivity().runOnUiThread(() -> {
                    String qrData = result.getText();

                    viewModel.resetState();

                    viewModel.parseQrCode(qrData);

                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_scanQr_to_main);
                });
            }
        });

        // 点击重新扫描
        view.findViewById(R.id.scanner_view).setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        String[] cameraPermission = PermissionUtils.getCameraPermissions();
        if (PermissionUtils.hasPermissions(requireContext(), cameraPermission)) {
            mCodeScanner.startPreview();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null && PermissionUtils.hasPermissions(requireContext(), PermissionUtils.getCameraPermissions())) {
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