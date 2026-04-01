package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.viewModel.UserViewModel;

public class SetFragment extends Fragment {
    private Button btnLogout;
    private Toolbar toolbar;
    private UserViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        btnLogout = view.findViewById(R.id.btn_logout);
        toolbar = view.findViewById(R.id.toolbar);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewModel.logout();
                MqttManager.getInstance().disconnect();
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.mainFragment, true) // 清空栈
                        .build();
                Navigation.findNavController(v).navigate(R.id.loginFragment, null, navOptions);


            }
        });
        toolbar.setNavigationOnClickListener(v -> {
            if (getView() != null) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btnLogout = null;
        toolbar = null;
    }
}
