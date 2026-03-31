package com.agonylua.smarthome.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agonylua.smarthome.adapter.HomeMemberAdapter;
import com.agonylua.smarthome.databinding.FragmentHomeManageBinding;
import com.agonylua.smarthome.viewModel.UserViewModel;

public class HomeManageFragment extends Fragment {

    private FragmentHomeManageBinding binding;
    private HomeMemberAdapter adapter;
    private UserViewModel viewModel;
    private boolean isCurrentUserOwner = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeManageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding.setViewModel(viewModel);
        initViews();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getHome().observe(getViewLifecycleOwner(), home -> {
            if (home != null) {
                binding.setHome(home);
                viewModel.refreshHomeData(home);
            }

        });

        viewModel.getBtnExitHome().observe(getViewLifecycleOwner(), canExit -> {
            binding.btnExitHome.setVisibility(canExit ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsOwner().observe(getViewLifecycleOwner(), isOwner -> {
            adapter.setIsCurrentUserOwner(isOwner);
        });

        viewModel.getMemberList().observe(getViewLifecycleOwner(), members -> {
            adapter.submitList(members);
        });

    }

    private void initViews() {
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.ivCopyId.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("HomeID", binding.tvHomeId.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "家庭 ID 已复制", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化 RecyclerView 与适配器
        adapter = new HomeMemberAdapter();
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMembers.setAdapter(adapter);

        // 监听来自 Adapter 的真正 "点击删除" 事件
        adapter.setOnMemberDeleteListener((position, member) -> {
            adapter.removeItem(position);
            viewModel.removeMember(member.userId);
        });

        // 普通成员主动退出家庭
        binding.btnExitHome.setOnClickListener(v -> {
            viewModel.exitHome();
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}