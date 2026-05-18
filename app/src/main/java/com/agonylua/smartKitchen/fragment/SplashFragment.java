package com.agonylua.smartKitchen.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.FragmentSplashBinding;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.network.WebSocketManager;
import com.agonylua.smartKitchen.repository.LoginRepository;
import com.agonylua.smartKitchen.viewModel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {
    private static final String TAG = "SplashFragment";
    private FragmentSplashBinding binding;
    private LoginViewModel viewModel;
    private LoginRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("ApiConfig", Context.MODE_PRIVATE);
        String newUrl = prefs.getString("baseUrl", "47.238.79.228:1234");
        //String newUrl = prefs.getString("baseUrl", "192.168.163.152:1234");
        updateApiUrl(newUrl);

        binding.ivLogo.setOnLongClickListener(v -> {
            Boolean isConnected = viewModel.getIsConnected().getValue();
            if (isConnected != null && !isConnected) {
                showEditUrlDialog();
            }
            return true;
        });

        playEntryAnimation();
        viewModel.tokenValidate();
        observeViewModel();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void observeViewModel() {
        NavController navController = Navigation.findNavController(requireView());

        viewModel.getIsNetwork().observe(getViewLifecycleOwner(), isNetwork -> {
            if (isNetwork != null && !isNetwork) {
                if (getView() != null) {
                    Log.d(TAG, "observeViewModel: 服务器连接失败，显示错误界面");
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.tvErrorIcon.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setVisibility(View.VISIBLE);
                }
            }
        });
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), valid -> {
            if (viewModel.getIsNetwork().getValue() != null && !viewModel.getIsNetwork().getValue()) {
                return;
            }
            if (valid != null && isAdded() && getView() != null) {
                if (valid) {
                    navController.navigate(R.id.action_splash_to_main);
                    viewModel.clearLoginResult();
                } else {
                    navController.navigate(R.id.action_splash_to_login);
                    viewModel.clearLoginResult();
                }
            }
        });

    }

    private void showEditUrlDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ApiConfig", Context.MODE_PRIVATE);
        String currentUrl = prefs.getString("baseUrl", com.agonylua.smartKitchen.network.RetrofitClient.IP_PORT);

        EditText editText = new EditText(requireContext());
        editText.setText(currentUrl);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_join_home, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // 使用 GradientDrawable 设置背景色及各个角的圆角弧度
        if (dialog.getWindow() != null) {
            android.graphics.drawable.GradientDrawable bgShape = new android.graphics.drawable.GradientDrawable();
            bgShape.setColor(android.graphics.Color.WHITE);
            bgShape.setCornerRadii(new float[]{40f, 40f, 40f, 40f, 40f, 40f, 40f, 40f});
            dialog.getWindow().setBackgroundDrawable(bgShape);
            dialogView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        // 获取内部控件并绑定事件
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvMessage = dialogView.findViewById(R.id.tv_dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.et_home_code);
        TextInputEditText etPostIP = (TextInputEditText) textInputLayout.getEditText();

        tvTitle.setText("修改服务器地址");
        tvMessage.setText("请输入新的服务器地址（IP:端口）");
        if (etPostIP != null) {
            etPostIP.setHint(currentUrl);
        }
        btnConfirm.setText("确认");

        btnCancel.setOnClickListener(v1 -> {
            dialog.dismiss();
        });
        btnConfirm.setOnClickListener(v2 -> {
            if (etPostIP != null && etPostIP.getText() != null && !etPostIP.getText().toString().trim().isEmpty()) {
                String newUrl = etPostIP.getText().toString().trim();
                prefs.edit().putString("baseUrl", newUrl).apply();
                updateApiUrl(newUrl);
                viewModel.retryConnection();
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateApiUrl(String newUrl) {
        RetrofitClient.IP_PORT = newUrl;
        RetrofitClient.resetInstance();
        WebSocketManager.IP_PORT = newUrl;
    }

    private void playEntryAnimation() {
        binding.llLogoContainer.setAlpha(0f);
        binding.llLogoContainer.setTranslationY(80f);
        binding.pbLoading.setAlpha(0f);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(binding.llLogoContainer, "alpha", 0f, 1f);
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(binding.llLogoContainer, "translationY", 80f, 0f);
        ObjectAnimator loadingAlphaAnimator = ObjectAnimator.ofFloat(binding.pbLoading, "alpha", 0f, 1f);

        loadingAlphaAnimator.setStartDelay(600);
        loadingAlphaAnimator.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translationYAnimator, loadingAlphaAnimator);
        animatorSet.setDuration(1200);
        animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
        animatorSet.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}