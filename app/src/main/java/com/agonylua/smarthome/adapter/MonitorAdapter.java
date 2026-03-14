package com.agonylua.smarthome.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.databinding.ItemMonitorCardBinding;

import java.util.ArrayList;
import java.util.List;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.DeviceViewHolder> {

    private List<Device> onlineDevices = new ArrayList<>();

    public void submitList(List<Device> devices) {
        this.onlineDevices = devices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用 DataBinding 自动生成的 Binding 类进行 inflate
        ItemMonitorCardBinding binding = ItemMonitorCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(onlineDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return onlineDevices == null ? 0 : onlineDevices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final ItemMonitorCardBinding binding;

        public DeviceViewHolder(ItemMonitorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Device device) {
            // 一行代码，搞定所有 UI 赋值与视图更新
            binding.setDevice(device);
            binding.executePendingBindings(); // 强制立即刷新绑定
        }
    }
}