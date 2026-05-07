package com.agonylua.smartKitchen.fragment;

import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.FragmentUserProfileBinding;
import com.agonylua.smartKitchen.model.User;
import com.agonylua.smartKitchen.utils.AvatarPickerHelper;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.UserViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    // 原始数据
    private String originalNickname = "";
    private boolean isSaveEnabled = false;
    private boolean isPasswordConsistent = false;
    // 状态记录：记录手风琴抽屉是否展开
    private boolean isNicknameExpanded = false;
    private boolean isPasswordExpanded = false;
    private UserViewModel viewModel;
    private AvatarPickerHelper avatarPickerHelper;
    private OnBackPressedCallback backPressedCallback; // 用于拦截系统返回键

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarPickerHelper = new AvatarPickerHelper(this, new AvatarPickerHelper.OnAvatarProcessListener() {

            @Override
            public void onProcessStart() {

            }

            @Override
            public void onSuccess(File compressedImageFile) {
                viewModel.setAvatarFile(compressedImageFile);
            }

            @Override
            public void onError(String errorMessage) {
                SnackbarUtils.show(binding.getRoot(), "头像选择失败: ");
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        binding.setViewModel(viewModel);

        viewModel.loadUserData();
        binding.setLifecycleOwner(getViewLifecycleOwner());
        setupListeners();
        setupBackPressedDispatcher();
        observeViewModel();

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetterOrDigit(c)) {
                        return ""; // 不是字母/数字，直接拦截
                    }
                }
                return null;
            }
        };
        binding.etNicknameInput.setFilters(new InputFilter[]{filter});
    }

    private void observeViewModel() {
        viewModel.getUsersDataList().observe(getViewLifecycleOwner(), data -> {
            User user = new User(data);
            binding.setUser(user);

            Glide.with(this)          // 绑定当前 Activity 的生命周期
                    .load(user.getAvatarUrl())                 // 传入图片链接
                    .placeholder(R.drawable.login_avatar) // (可选) 设置正在下载时显示的默认头像
                    .fallback(R.drawable.login_avatar)
                    .error(R.drawable.ic_launcher_foreground)         // (可选) 设置下载失败时显示的图片
                    .circleCrop()                    // 再次确保图片内容被裁剪为圆形
                    .into(binding.ivAvatar);          // 渲染到控件上
        });

        viewModel.getIsSaveEnabled().observe(getViewLifecycleOwner(), enabled -> {
            binding.btnSave.setEnabled(enabled);
            isSaveEnabled = enabled;
        });
        viewModel.getIsUserInfoResult().observe(getViewLifecycleOwner(), result -> {
            if (result) {
                navigateUpSafely();
                viewModel.setIsUserInfoResult(false);
            }
        });
    }

    private void setupListeners() {
        // 头像点击
        View.OnClickListener avatarClickListener = v -> avatarPickerHelper.startSelection();
        binding.ivAvatar.setOnClickListener(avatarClickListener);
        binding.btnEditAvatar.setOnClickListener(avatarClickListener);

        // 昵称行手风琴切换
        binding.rowNickname.setOnClickListener(v -> toggleNicknameExpand());

        // 密码行手风琴切换
        binding.rowPassword.setOnClickListener(v -> togglePasswordExpand());

        // 返回键与保存键
        binding.toolbar.setNavigationOnClickListener(v -> handleExit());
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    /**
     * 切换昵称区域展开状态并旋转箭头
     */
    private void toggleNicknameExpand() {
        isNicknameExpanded = !isNicknameExpanded;
        if (isNicknameExpanded) {
            binding.layoutExpandNickname.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(binding.ivArrowNickname, "rotation", 0f, 90f).setDuration(200).start();
        } else {
            binding.layoutExpandNickname.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(binding.ivArrowNickname, "rotation", 90f, 0f).setDuration(200).start();
        }
    }

    /**
     * 切换密码区域展开状态并旋转箭头
     */
    private void togglePasswordExpand() {
        isPasswordExpanded = !isPasswordExpanded;
        if (isPasswordExpanded) {
            binding.layoutExpandPassword.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(binding.ivArrowPassword, "rotation", 0f, 90f).setDuration(200).start();
        } else {
            binding.layoutExpandPassword.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(binding.ivArrowPassword, "rotation", 90f, 0f).setDuration(200).start();
        }
    }

    private void setupBackPressedDispatcher() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    private void handleExit() {
        if (isSaveEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_confirm, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // 必须将 Dialog 窗口背景设为透明，否则圆角四个角会有白色方块底色
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            // 获取内部控件并绑定事件
            MaterialButton btnNegative = dialogView.findViewById(R.id.btn_dialog_negative);
            MaterialButton btnPositive = dialogView.findViewById(R.id.btn_dialog_positive);

            // 点击“继续编辑”（灰色按钮）：直接关闭弹窗即可
            btnNegative.setOnClickListener(v -> dialog.dismiss());

            // 点击“放弃并退出”（红色按钮）：关闭弹窗并返回上一页
            btnPositive.setOnClickListener(v -> {
                dialog.dismiss();
                navigateUpSafely();
            });

            dialog.show();

        } else {
            navigateUpSafely();
        }
    }

    private void saveProfile() {
        isPasswordConsistent = viewModel.newPasswordChecks();
        if (isPasswordConsistent) {
            binding.tvPasswordError.setVisibility(View.GONE);
            viewModel.saveProfile();
            binding.etOldPassword.setText("");
            binding.etNewPassword.setText("");
            binding.etConfirmPassword.setText("");


            // 收起所有手风琴面板
            if (isNicknameExpanded) toggleNicknameExpand();
            if (isPasswordExpanded) togglePasswordExpand();
        } else {
            binding.tvPasswordError.setVisibility(View.VISIBLE);
        }
    }

    private void navigateUpSafely() {
        if (isAdded() && getView() != null) {
            Navigation.findNavController(getView()).navigateUp();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}