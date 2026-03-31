package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.databinding.FragmentUserProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    // 缓存进入页面时的原始数据，用于对比是否发生修改
    private String originalNickname = "";
    private String originalPhone = "";
    private String originalSignature = "";
    private boolean isAvatarChanged = false; // 标记头像是否被替换过

    // 系统返回键拦截器
    private OnBackPressedCallback backPressedCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserData();
        setupListeners();
        setupBackPressedDispatcher();
    }

    /**
     * 加载当前用户的已有数据
     */
    private void loadUserData() {
        // TODO: 从 ViewModel 或 UserManager 获取真实数据
        originalNickname = "AgonyLua";
        originalPhone = "13800000000";
        originalSignature = "热爱智能生活";

        // 初始状态下禁用保存按钮
        updateSaveButtonState();
    }

    private void setupListeners() {
        // 1. 头像点击事件
        View.OnClickListener avatarClickListener = v -> {
            // TODO: 调用系统相册或相机获取图片
            Toast.makeText(requireContext(), "打开相册选择头像...", Toast.LENGTH_SHORT).show();
            // 模拟更换头像成功
            isAvatarChanged = true;
            updateSaveButtonState();
        };
        binding.ivAvatar.setOnClickListener(avatarClickListener);
        binding.btnEditAvatar.setOnClickListener(avatarClickListener);

        // 2. 文本修改监听器
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 每次输入变化，实时检测是否与原数据不同
                updateSaveButtonState();
            }
        };

        // 3. 左上角返回按钮
        binding.ivBack.setOnClickListener(v -> handleExit());

        // 4. 保存按钮
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    /**
     * 判断表单是否有未保存的修改
     */
//    private boolean hasUnsavedChanges() {
//        String currentNickname = binding.etNickname.getText().toString().trim();
//        String currentPhone = binding.etPhone.getText().toString().trim();
//        String currentSignature = binding.etSignature.getText().toString().trim();
//
//        return isAvatarChanged ||
//                !currentNickname.equals(originalNickname) ||
//                !currentPhone.equals(originalPhone) ||
//                !currentSignature.equals(originalSignature);
//    }

    /**
     * 动态更新保存按钮的点亮状态
     */
    private void updateSaveButtonState() {
//        binding.btnSave.setEnabled(hasUnsavedChanges());
    }

    /**
     * 设置系统物理返回键和手势返回的拦截
     */
    private void setupBackPressedDispatcher() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    /**
     * 统一的退出处理逻辑：检查是否需要弹窗
     */
    private void handleExit() {
//        if (hasUnsavedChanges()) {
//            showUnsavedChangesDialog();
//        } else {
//            navigateUpSafely();
//        }
    }

    /**
     * 弹出“放弃修改”警告框
     */
    private void showUnsavedChangesDialog() {
        new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
                .setTitle("未保存的修改")
                .setMessage("您更改了部分个人资料但尚未保存，现在退出将丢失这些修改。")
                .setPositiveButton("继续编辑", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("放弃并退出", (dialog, which) -> navigateUpSafely())
                .show();
    }

    /**
     * 执行保存操作
     */
    private void saveProfile() {
        // TODO: 收集数据并调用 ViewModel 提交给后端
        Toast.makeText(requireContext(), "资料已保存", Toast.LENGTH_SHORT).show();

        // 模拟保存成功，重置缓存数据，防止返回时误报
//        originalNickname = binding.etNickname.getText().toString().trim();
//        originalPhone = binding.etPhone.getText().toString().trim();
//        originalSignature = binding.etSignature.getText().toString().trim();
//        isAvatarChanged = false;

        updateSaveButtonState(); // 重新禁用保存按钮

        // 保存成功后可选：自动返回上级页面
        binding.getRoot().postDelayed(this::navigateUpSafely, 500);
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
