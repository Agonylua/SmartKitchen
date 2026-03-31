package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentUserBinding;
import com.agonylua.smarthome.model.User;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.UserViewModel;
import com.bumptech.glide.Glide;

public class UserFragment extends Fragment {
    private TextView btn_settings;
    private final String TAG = "UserFragment";
    private UserManager userManager;
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

        binding.btHomeSettings.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.homeManageFragment));
        userManager = UserManager.getInstance(requireActivity().getApplication());
        binding.setLifecycleOwner(getViewLifecycleOwner());
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
        userViewModel.getIsLogin().observe(getViewLifecycleOwner(), isLogin -> {
            if (!isLogin && getView() != null) {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.mainFragment, true) // 清空栈
                        .build();

                Navigation.findNavController(getView())
                        .navigate(R.id.loginFragment, null, navOptions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
