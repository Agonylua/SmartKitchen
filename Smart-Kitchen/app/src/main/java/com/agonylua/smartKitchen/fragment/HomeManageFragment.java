package com.agonylua.smartKitchen.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.adapter.HomeMemberAdapter;
import com.agonylua.smartKitchen.databinding.FragmentHomeManageBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.viewModel.UserViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeManageFragment extends Fragment {

    private FragmentHomeManageBinding binding;
    private HomeMemberAdapter adapter;
    private UserViewModel viewModel;

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

        viewModel.getExitHomeResult().observe(getViewLifecycleOwner(), result -> {
            if (result) {
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack();
                viewModel.setExitHomeResult(false);
            }
        });

    }

    private void initViews() {
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.ivCopyId.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("HomeID", binding.tvHomeId.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                SnackbarUtils.show(requireView(), "家庭 ID 已复制到剪贴板");
            }
        });

        // 初始化 RecyclerView 与适配器
        adapter = new HomeMemberAdapter();
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMembers.setAdapter(adapter);

        // 监听来自 Adapter 的真正 "点击删除" 事件
        adapter.setOnMemberDeleteListener((position, member) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_confirm, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
            ivIcon.setImageResource(R.drawable.error_info);

            TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
            tvTitle.setText("确认删除成员");
            TextView tvMessage = dialogView.findViewById(R.id.tv_dialog_message);
            tvMessage.setText("您确定要将 " + member.username + " 从家庭中删除吗？此操作无法撤销。");

            // 获取内部控件并绑定事件
            MaterialButton btnNegative = dialogView.findViewById(R.id.btn_dialog_negative);
            MaterialButton btnPositive = dialogView.findViewById(R.id.btn_dialog_positive);

            btnNegative.setText("取消");
            btnPositive.setText("确认删除");

            // 点击“继续编辑”（灰色按钮）：直接关闭弹窗即可
            btnNegative.setOnClickListener(v -> dialog.dismiss());

            // 点击“放弃并退出”（红色按钮）：关闭弹窗并返回上一页
            btnPositive.setOnClickListener(v -> {
                dialog.dismiss();
                adapter.removeItem(position);
                viewModel.removeMember(member.userId);
            });

            dialog.show();

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