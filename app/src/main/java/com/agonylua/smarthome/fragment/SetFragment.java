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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.viewModel.SetViewModel;

public class SetFragment extends Fragment {
    private Button btnLogout;
    private Toolbar toolbar;
    private SetViewModel setViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViewModel = new ViewModelProvider(this).get(SetViewModel.class);
        btnLogout = view.findViewById(R.id.btn_logout);
        toolbar = view.findViewById(R.id.toolbar);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setViewModel.logout();
                MqttManager.getInstance().disconnect();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.loginFragment);
                }
            }
        });
        toolbar.setNavigationOnClickListener(v -> {
            if (getView() != null) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }


}
