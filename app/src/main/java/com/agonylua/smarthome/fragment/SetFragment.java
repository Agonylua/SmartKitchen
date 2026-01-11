package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.ViewModel.SetViewModel;

public class SetFragment extends Fragment {
    private Button btnLogout;
    private SetViewModel setViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        btnLogout = root.findViewById(R.id.btn_logout);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewModel.Cancellation();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.mainFragment);
                }
            }
        });
        return root;
    }


}
