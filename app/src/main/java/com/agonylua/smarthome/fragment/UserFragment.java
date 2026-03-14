package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentUserBinding;
import com.agonylua.smarthome.model.User;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.UserViewModel;

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
        obServeViewModel();
        userViewModel.loadUserData();

//        binding.setMoreSettings((v) -> {
//            Navigation.findNavController(v).navigate(R.id.setFragment);
//        });
        binding.setViewModel(userViewModel);
        userManager = UserManager.getInstance(requireContext());
        Log.v(TAG, "User Info: " + userManager.getData());
        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    private void obServeViewModel() {
        userViewModel.getUsersDataList().observe(getViewLifecycleOwner(), data -> {
            Log.v(TAG, "User Data Updated: " + data);
            User user = new User(data);
            binding.setUser(user);
        });
        userViewModel.getIsLogin().observe(getViewLifecycleOwner(), isLogin -> {
            if (!isLogin && getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.loginFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
