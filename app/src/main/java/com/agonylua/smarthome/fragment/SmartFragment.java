package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.databinding.FragmentSmartBinding;

public class SmartFragment extends Fragment {

    private FragmentSmartBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSmartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.fabAddRule.setOnClickListener(v -> {
            // 这里可以直接使用 NavController 来导航到添加规则的界面
            NavHostFragment.findNavController(SmartFragment.this).navigate(R.id.addRuleBottomSheetFragment);
        });
    }
}
