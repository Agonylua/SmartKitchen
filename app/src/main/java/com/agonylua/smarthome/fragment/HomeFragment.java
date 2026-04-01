package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.adapter.DeviceAdapter;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.databinding.FragmentHomeBinding;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String homeId;
    private UserManager userManager;
    private DeviceAdapter adapter;
    private String TAG = "HomeFragment";

    // 引入 ViewBinding
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用 DataBinding/ViewBinding 渲染布局
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化基础数据
        userManager = UserManager.getInstance(requireActivity().getApplication());
        homeId = userManager.getHomeId();
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 绑定 ViewModel 到 XML 以支持 DataBinding
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        setupRecyclerView();
        setupRefreshLayout();
        observeViewModel();

        homeViewModel.syncServiceData(homeId);

        // 替代原有 Toolbar 的标题逻辑，渲染新 UI 的沉浸式大标题
        String nickname = userManager.getNickName() != null ? userManager.getNickName() : "我";
        nickname = nickname.length() > 5 ? nickname.substring(0, 4) + "..." : nickname;
        binding.tvGreeting.setText(nickname + "的厨房");

        binding.ivAddDevice.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.scanQrFragment);
        });
    }

    private void setupRecyclerView() {
        // 适配重构后的 DeviceAdapter
        adapter = new DeviceAdapter();
        binding.rvDevices.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvDevices.setAdapter(adapter);

        // 适配新的点击事件监听器
        adapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Device device) {
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                MainFragmentDirections.ActionMainFragmentToDeviceFragment2 action =
                        MainFragmentDirections.actionMainFragmentToDeviceFragment2(device);
                navController.navigate(action);
            }
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
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error: " + msg);
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