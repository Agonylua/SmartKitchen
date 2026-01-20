package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.adapter.DeviceAdapter;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.utils.UserManager;
import com.agonylua.smarthome.viewModel.HomeViewModel;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView rvDevices;
    private Toolbar toolbar;
    private TextView tvDeviceCount, tvTitle;
    private String homeId;
    private UserManager userManager;
    private DeviceAdapter adapter;
    private SmartRefreshLayout refreshLayout;
    private String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化
        init(view);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // 设置RecyclerView
        adapter = new DeviceAdapter(getContext());
        rvDevices.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 网格布局，一行2个
        rvDevices.setAdapter(adapter);
        adapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(Device device) {
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                MainFragmentDirections.ActionMainFragmentToDeviceFragment2 action =
                        MainFragmentDirections.actionMainFragmentToDeviceFragment2(device);
                if (action != null) {
                    navController.navigate(action);
                }
            }
        });
        // 下拉刷新监听
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                homeViewModel.loadDevices("d6J5Gp");
            }
        });

        observeViewModel();

        //TODO: 这里的 homeId 需要动态获取
        homeViewModel.loadDevices(homeId);
        String nickname = userManager.getNickName() + "的智能家居";
        toolbar.setTitle(nickname);
    }

    private void init(View view) {
        toolbar = view.findViewById(R.id.topbar);
        rvDevices = view.findViewById(R.id.rv_devices);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        userManager = UserManager.getInstance(getContext());
        homeId = userManager.getHomeId();
    }

    private void observeViewModel() {
        // 观察列表数据变化
        homeViewModel.getDeviceList(homeId).observe(getViewLifecycleOwner(), devices -> {
            if (devices != null) {
                adapter.setDeviceList(devices);
            }
            refreshLayout.finishRefresh(true);
        });

        // 观察错误信息
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "错误信息: " + msg);
            refreshLayout.finishRefresh(false);
        });

        homeViewModel.getDeviceCount().observe(getViewLifecycleOwner(), count -> {
            count = " " + count + " 台设备";
            toolbar.setSubtitle(count);
        });
    }
}
