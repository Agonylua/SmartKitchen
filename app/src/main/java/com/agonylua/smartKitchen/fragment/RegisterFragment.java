package com.agonylua.smartKitchen.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.databinding.FragmentRegisterBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.LoginViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // 左上角返回
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 底部去登录
        binding.tvGoLogin.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 注册按钮点击事件
        binding.btnRegister.setOnClickListener(v -> {
            String account = binding.etAccount.getText() != null ? binding.etAccount.getText().toString().trim() : "";
            String nickname = binding.etNickname.getText() != null ? binding.etNickname.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

            // 客户端表单校验
            boolean hasError = false;

            if (TextUtils.isEmpty(account)) {
                binding.tilAccount.setError("账号不能为空");
                hasError = true;
            } else {
                binding.tilAccount.setError(null);
            }

            if (TextUtils.isEmpty(nickname)) {
                binding.tilNickname.setError("昵称不能为空");
                hasError = true;
            } else {
                binding.tilNickname.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                binding.tilPassword.setError("密码不能为空");
                hasError = true;
            } else if (password.length() < 6) {
                binding.tilPassword.setError("密码长度至少为 6 位");
                hasError = true;
            } else {
                binding.tilPassword.setError(null);
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                binding.tilConfirmPassword.setError("请确认密码");
                hasError = true;
            } else if (!password.equals(confirmPassword)) {
                binding.tilConfirmPassword.setError("两次输入的密码不一致");
                hasError = true;
            } else {
                binding.tilConfirmPassword.setError(null);
            }

            if (hasError) return;

            // 校验通过，隐藏软键盘并开始注册
            hideKeyboard();
            viewModel.register(account, nickname, password);
        });
    }

    private void observeViewModel() {
        // 监听注册结果
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                SnackbarUtils.show(requireView(), "注册成功，请登录");

                // 注册成功后返回登录页面
                if (getView() != null) {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                }
            }
        });

        // 监听错误信息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                SnackbarUtils.show(requireView(), errorMsg);
                viewModel.clearErrorMessage();
            }
        });

        // 监听加载状态控制表单禁用
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                binding.tilAccount.setAlpha(0.6f);
                binding.tilNickname.setAlpha(0.6f);
                binding.tilPassword.setAlpha(0.6f);
                binding.tilConfirmPassword.setAlpha(0.6f);

                binding.tilAccount.setEnabled(false);
                binding.tilNickname.setEnabled(false);
                binding.tilPassword.setEnabled(false);
                binding.tilConfirmPassword.setEnabled(false);
            } else {
                binding.tilAccount.setAlpha(1.0f);
                binding.tilNickname.setAlpha(1.0f);
                binding.tilPassword.setAlpha(1.0f);
                binding.tilConfirmPassword.setAlpha(1.0f);

                binding.tilAccount.setEnabled(true);
                binding.tilNickname.setEnabled(true);
                binding.tilPassword.setEnabled(true);
                binding.tilConfirmPassword.setEnabled(true);
            }
        });
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
        binding = null; // 避免内存泄漏
    }
}