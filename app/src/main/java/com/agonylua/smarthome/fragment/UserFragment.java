package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.agonylua.smarthome.R;

public class UserFragment extends Fragment {
    private TextView btn_logout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View user = inflater.inflate(R.layout.fragment_user, container, false);
        btn_logout = user.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理登出逻辑，例如清除用户数据，跳转到登录界面等
            }
        });
        return user;
    }
}
