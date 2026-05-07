package com.agonylua.smartKitchen.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.databinding.ItemMonitorCardBinding;
import com.agonylua.smartKitchen.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.DeviceViewHolder> {

    private List<Device> onlineDevices = new ArrayList<>();
    private static final String TAG = "MonitorAdapter";

    public void submitList(List<Device> devices) {
        this.onlineDevices = devices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
            binding.setDevice(device);
            String runTime = TimeUtils.formatToDdHhMm(device.getRunTime());
            binding.runTime.setText(runTime);
            binding.executePendingBindings(); // 强制立即刷新绑定
        }
    }
}