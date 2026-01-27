package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.viewModel.SplashViewModel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SplashFragment extends Fragment {
    private SplashViewModel splashViewModel;
    private ProgressBar progressBar;

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
        progressBar = view.findViewById(R.id.ic_loading);
        // 开始检查
        splashViewModel.LoginCheck();
        observeViewModel();
    }

    private void observeViewModel() {
        showLoading();
        splashViewModel.getTokenValid().observe(getViewLifecycleOwner(), tokenValid -> {
            if (tokenValid == 1) {
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                }
            } else if (tokenValid == 0) {
                Toast.makeText(getContext(), "身份验证失败，请重新登录。", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(this.requireView()).navigate(R.id.loginFragment);
            } else if (tokenValid == -1) {
                if (getView() != null) {
                    // TODO: 这里需要添加离线情况的处理逻辑
                    Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                }
            }
        });

    }

    // 显示加载状态
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }
}
