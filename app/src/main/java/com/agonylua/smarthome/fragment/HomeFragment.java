package com.agonylua.smarthome.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.ViewModel.HomeViewModel;
import com.agonylua.smarthome.adapter.DeviceAdapter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView rvDevices;
    private DeviceAdapter adapter;
    private SmartRefreshLayout refreshLayout;
    private String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化
        rvDevices = root.findViewById(R.id.rv_devices);
        refreshLayout = root.findViewById(R.id.refreshLayout);

        adapter = new DeviceAdapter(getContext());
        rvDevices.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 网格布局，一行2个
        rvDevices.setAdapter(adapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                homeViewModel.loadDevices("d6J5Gp");
            }
        });

        observeViewModel();

        //TODO: 这里的 homeId 需要动态获取
        homeViewModel.loadDevices("d6J5Gp");
        Log.d(TAG, "onCreateView: 请求设备列表");
        return root;
    }

    private void observeViewModel() {
        // 观察列表数据变化
        homeViewModel.getDeviceList().observe(getViewLifecycleOwner(), devices -> {
            if (devices != null) {
                adapter.setDeviceList(devices);
            }
            refreshLayout.finishRefresh(true);
        });

        // 观察错误信息
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            refreshLayout.finishRefresh(false);
        });
    }
}
