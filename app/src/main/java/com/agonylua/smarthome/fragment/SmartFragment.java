package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.adapter.SmartAdapter;
import com.agonylua.smarthome.database.entity.Rules;
import com.agonylua.smarthome.databinding.FragmentSmartBinding;
import com.agonylua.smarthome.repository.SmartRepository;
import com.agonylua.smarthome.viewModel.SmartViewModel;

public class SmartFragment extends Fragment {

    private FragmentSmartBinding binding;
    private static final String TAG = "SmartFragment";
    private SmartViewModel viewModel;
    private SmartRepository repository;
    private SmartAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSmartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SmartViewModel.class);
        repository = SmartRepository.getInstance(requireActivity().getApplication());
        viewModel.init(repository);
        adapter = new SmartAdapter();

        binding.setViewModel(viewModel);
        initListener();
        observeViewModel();

        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            viewModel.syncRulesList();
        });
    }

    private void initListener() {
        // 设置监听器处理长按删除
        adapter.setOnItemClickListener(new SmartAdapter.OnSceneActionListener() {
            @Override
            public void onExecutePreset(Rules rule) {

            }

            @Override
            public void onToggleCustomRule(Rules rule, boolean isChecked) {

            }

            @Override
            public void onDeleteRule(Rules rule) {
                viewModel.deleteRule(rule.getRuleId());
            }
        });
        binding.rvRules.setAdapter(adapter);

        // 如果用户在滚动列表，也应当清除当前的删除确认状态
        binding.rvRules.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int newState) {
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING && adapter != null) {
                    adapter.clearDeleteMode();
                }
            }
        });

        // 如果用户点击列表外面的空白区域（例如 NestedScrollView 的其他部分），也清除删除状态
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (adapter != null) {
                    adapter.clearDeleteMode();
                }
            }
            return false;
        });

        binding.fabAddRule.setOnClickListener(v -> NavHostFragment.findNavController(SmartFragment.this).navigate(R.id.customRulesFragment));

        binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    public void observeViewModel() {
        viewModel.getRulesList().observe(getViewLifecycleOwner(), rules -> {
            adapter.submitList(rules);
        });
        viewModel.refreshResult.observe(getViewLifecycleOwner(), result -> {
            if (result) {
                binding.refreshLayout.finishRefresh(true);
            } else {
                binding.refreshLayout.finishRefresh(false);
            }

        });
    }
}
