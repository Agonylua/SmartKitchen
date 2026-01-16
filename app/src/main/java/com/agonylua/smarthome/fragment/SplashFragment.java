package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.Utils.NetworkUtils;
import com.agonylua.smarthome.ViewModel.SplashViewModel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SplashFragment extends Fragment {
    private NetworkUtils networkUtils;
    private SplashViewModel splashViewModel;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private TextView tvStatus;
    private TextView tvErrorMsg;
    private Button btnRetry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        // 绑定控件
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutError = view.findViewById(R.id.layout_error);
        tvStatus = view.findViewById(R.id.tv_loading_status);
        tvErrorMsg = view.findViewById(R.id.tv_error_msg);
        btnRetry = view.findViewById(R.id.btn_retry);
        // 开始检查
        splashViewModel.LoginCheck();
        // 设置重试按钮点击事件
        btnRetry.setOnClickListener(v -> splashViewModel.LoginCheck());
        observeViewModel();
    }

    private void observeViewModel() {
        showLoading("正在检查网络环境...");
        splashViewModel.getHasNetwork().observe(getViewLifecycleOwner(), hasNetwork -> {
            if (!hasNetwork) {
                showError("网络不可用，请检查您的连接。");
            }
        });
        showLoading("正在进行身份验证...");
        splashViewModel.getTokenValid().observe(getViewLifecycleOwner(), tokenValid -> {
            if (tokenValid) {
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                }
            } else {
                Toast.makeText(getContext(), "身份验证失败，请重新登录。", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(this.requireView()).navigate(R.id.loginFragment);
            }
        });

    }

    // 显示加载状态
    private void showLoading(String message) {
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        tvStatus.setText(message);
    }

    // 显示错误状态
    private void showError(String errorMsg) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMsg.setText(errorMsg);
    }
}
