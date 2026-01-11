package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;

public class UserFragment extends Fragment {
    private TextView btn_settings;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View user = inflater.inflate(R.layout.fragment_user, container, false);
        btn_settings = user.findViewById(R.id.bt_settings);
        btn_settings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.setFragment);
        });
        return user;
    }
}
