package com.agonylua.smartKitchen.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.databinding.FragmentUserBinding;
import com.agonylua.smartKitchen.model.User;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.utils.UserManager;
import com.agonylua.smartKitchen.viewModel.UserViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserFragment extends Fragment {
    private TextView btn_settings;
    private final String TAG = "UserFragment";

    @Inject
    public UserManager userManager;

    private UserViewModel userViewModel;
    private FragmentUserBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        binding.setViewModel(userViewModel);
        obServeViewModel();
        userViewModel.loadUserData();

        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.btHomeManager.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_main_to_homeManage);
        });
        binding.btUserInfo.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_main_to_userProfile);
        });
        binding.btAbout.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        });
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> userViewModel.refreshUserData());
        binding.btJoinHome.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_join_home, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // 【关键黑科技】使用 GradientDrawable 设置背景色及各个角的圆角弧度
            if (dialog.getWindow() != null) {
                android.graphics.drawable.GradientDrawable bgShape = new android.graphics.drawable.GradientDrawable();
                bgShape.setColor(android.graphics.Color.WHITE);
                bgShape.setCornerRadii(new float[]{40f, 40f, 40f, 40f, 40f, 40f, 40f, 40f});
                dialog.getWindow().setBackgroundDrawable(bgShape);
                dialogView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
            // 获取内部控件并绑定事件
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);
            TextInputLayout textInputLayout = dialogView.findViewById(R.id.et_home_code);
            TextInputEditText etHomeId = (TextInputEditText) textInputLayout.getEditText();

            btnCancel.setOnClickListener(v1 -> {
                dialog.dismiss();
            });
            btnConfirm.setOnClickListener(v2 -> {
                String homeId = etHomeId != null ? etHomeId.getText().toString() : "";
                userViewModel.joinHome(homeId);
                dialog.dismiss();
            });
            dialog.show();
        });
        binding.btnLogout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_confirm, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // 【关键黑科技】必须将 Dialog 窗口背景设为透明，否则圆角四个角会有白色方块底色
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            // 获取内部控件并绑定事件
            MaterialButton btnNegative = dialogView.findViewById(R.id.btn_dialog_negative);
            MaterialButton btnPositive = dialogView.findViewById(R.id.btn_dialog_positive);
            ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
            TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
            TextView tvMessage = dialogView.findViewById(R.id.tv_dialog_message);
            btnNegative.setText("取消");
            btnPositive.setText("确认");
            ivIcon.setImageResource(R.drawable.ic_warning);
            tvTitle.setText("确认退出登录吗？");
            tvMessage.setText("");
            btnNegative.setOnClickListener(v1 -> dialog.dismiss());

            btnPositive.setOnClickListener(v2 -> {
                userViewModel.logout();
                dialog.dismiss();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_global_to_login);
            });
            dialog.show();
        });
    }

    private void obServeViewModel() {
        userViewModel.getUsersDataList().observe(getViewLifecycleOwner(), data -> {
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
        userViewModel.refreshResult.observe(getViewLifecycleOwner(), result -> {
            binding.refreshLayout.finishRefresh(result);
        });

        userViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                SnackbarUtils.show(requireView(), message);
                userViewModel.toastMessage.setValue(null); // consume message
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
