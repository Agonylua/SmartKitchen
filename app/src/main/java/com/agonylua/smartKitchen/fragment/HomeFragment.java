package com.agonylua.smartKitchen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.adapter.DeviceAdapter;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.databinding.FragmentHomeBinding;
import com.agonylua.smartKitchen.utils.SnackbarUtils;
import com.agonylua.smartKitchen.utils.UserManager;
import com.agonylua.smartKitchen.viewModel.HomeViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String homeId;

    @Inject
    public UserManager userManager;

    private DeviceAdapter adapter;
    private String TAG = "HomeFragment";

    // 引入 ViewBinding
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化基础数据
        homeId = userManager.getHomeId();
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 绑定 ViewModel 到 XML 以支持 DataBinding
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupRecyclerView();
        setupRefreshLayout();
        observeViewModel();

        homeViewModel.syncServiceData(homeId);

        String nickname = userManager.getNickName() != null ? userManager.getNickName() : "我";
        nickname = nickname.length() > 5 ? nickname.substring(0, 4) + "..." : nickname;
        binding.tvGreeting.setText(nickname + "的厨房");

        binding.ivAddDevice.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_main_to_scanQr);
        });
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter();
        binding.rvDevices.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvDevices.setAdapter(adapter);

        // 适配新的点击事件监听器
        adapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Device device) {
                adapter.clearDeleteMode();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(MainFragmentDirections.actionMainToDevice(device));
            }
        });

        // 适配长按删除事件监听器
        adapter.setOnItemLongClickListener(new DeviceAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Device device) {
                homeViewModel.deleteDevice(device.getDeviceSn(), homeId);
            }
        });

        // 为外层容器添加点击监听器，如果点击空白区域也能清除卡片的删除遮罩
        binding.refreshLayout.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.clearDeleteMode();
            }
        });

        binding.rvDevices.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (adapter != null) {
                    adapter.clearDeleteMode();
                }
            }
            return false;
        });
    }

    private void setupRefreshLayout() {
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            homeViewModel.syncServiceData(homeId);
        });
    }

    private void observeViewModel() {
        // 观察列表数据变化
        homeViewModel.getDeviceList(homeId).observe(getViewLifecycleOwner(), devices -> {
            if (devices != null) {
                adapter.submitList(devices); // 适配新 Adapter 的 submitList 方法
            }
        });

        // 观察错误信息
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                SnackbarUtils.show(requireActivity().findViewById(android.R.id.content), msg);
            }
            binding.refreshLayout.finishRefresh(false);
        });

        homeViewModel.getDeviceCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvWeather.setText("在线设备 · 共 " + count + " 台");
        });

        homeViewModel.getIsRefresh().observe(getViewLifecycleOwner(), isRefresh -> {
            binding.refreshLayout.finishRefresh(isRefresh);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
