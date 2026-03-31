package com.agonylua.smarthome.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentLoginBinding;
import com.agonylua.smarthome.repository.LoginRepository;
import com.agonylua.smarthome.viewModel.LoginViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;
    private LoginRepository loginRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        binding.setViewModel(loginViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // 登录按钮点击事件
        binding.btnLogin.setOnClickListener(v -> {
            String account = binding.etAccount.getText() != null ? binding.etAccount.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            // 表单校验
            if (TextUtils.isEmpty(account)) {
                binding.tilAccount.setError("账号不能为空");
                return;
            } else {
                binding.tilAccount.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                binding.tilPassword.setError("密码不能为空");
                return;
            } else {
                binding.tilPassword.setError(null);
            }

            // 隐藏软键盘
            hideKeyboard();

            // 切换为加载状态
            setLoadingState(true);

            // 发起登录请求 (假设 ViewModel 中有此方法)
            loginViewModel.login(account, password);
        });

        // 注册跳转
        binding.tvGoRegister.setOnClickListener(v -> {
            // TODO: 跳转到注册 Fragment
            // Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
            Toast.makeText(requireContext(), "跳转到注册页", Toast.LENGTH_SHORT).show();
        });

        // 忘记密码
        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "请联系管理员重置密码", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        // 监听登录结果状态 (请确保你的 LoginViewModel 暴露了对应的 LiveData)
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), isSuccess -> {
            setLoadingState(false);
            if (isSuccess != null && isSuccess) {
                Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show();
                // 登录成功后跳转到主页 MainFragment (即包含底部导航栏的宿主)
                if (getView() != null) {
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.splashFragment, true) // 清空栈
                            .build();

                    Navigation.findNavController(getView())
                            .navigate(R.id.mainFragment, null, navOptions);
                }
            }
        });

        // 监听错误信息
        loginViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                setLoadingState(false);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                // 清空错误信息防重弹
                loginViewModel.clearErrorMessage();
            }
        });
    }

    /**
     * 切换界面的加载状态，防止重复点击
     */
    private void setLoadingState(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.etAccount.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);

        if (isLoading) {
            binding.btnLogin.setText("登录中...");
            binding.llLoading.setVisibility(View.VISIBLE);
            binding.tilAccount.setAlpha(0.6f);
            binding.tilPassword.setAlpha(0.6f);
        } else {
            binding.btnLogin.setText("登 录");
            binding.llLoading.setVisibility(View.GONE);
            binding.tilAccount.setAlpha(1.0f);
            binding.tilPassword.setAlpha(1.0f);
        }
    }

    /**
     * 隐藏软键盘，提升用户体验
     */
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
        binding = null; // 释放 binding 避免内存泄漏
    }
}