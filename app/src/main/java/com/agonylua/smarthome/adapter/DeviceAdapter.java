package com.agonylua.smarthome.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.entity.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 更新数据列表，配合 ViewModel 的 observe 自动刷新
    public void submitList(List<Device> newDevices) {
        this.deviceList = newDevices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 绑定你最新的极简网格卡片布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    // 定义点击事件接口，方便在 Fragment 中处理页面跳转
    public interface OnItemClickListener {
        void onItemClick(Device device);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDeviceIcon;
        TextView tvDeviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeviceIcon = itemView.findViewById(R.id.iv_device_icon);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
        }

        public void bind(Device device, OnItemClickListener listener) {
            // 设置设备名称
            tvDeviceName.setText(device.getDeviceName() != null ? device.getDeviceName() : "未知设备");

            // 判断设备是否在线
            boolean isOnline = device.getDeviceStatus() != null && device.getDeviceStatus().equals("ONLINE");
            String deviceType = device.getDeviceType() != null ? device.getDeviceType() : "";

            // 根据设备类型和在线状态加载不同的图标
            ivDeviceIcon.setImageResource(getIconForDevice(deviceType, isOnline));

            // 处理卡片点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(device);
                }
            });
        }

        /**
         * 动态获取设备图标
         *
         * @param type     设备类型字符串
         * @param isOnline 是否在线
         * @return Drawable Resource ID
         */
        private int getIconForDevice(String type, boolean isOnline) {
            if (type.contains("REFRIGERATOR")) {
                return isOnline ? R.drawable.ic_device_refrigerator_online : R.drawable.ic_device_refrigerator_offline;
            } else if (type.contains("MICROWAVE")) {
                return isOnline ? R.drawable.ic_device_microwave_online : R.drawable.ic_device_microwave_offline;
            } else if (type.contains("OVEN")) {
                return isOnline ? R.drawable.ic_device_oven_online : R.drawable.ic_device_oven_offline;
            } else if (type.contains("DISHWASHER")) {
                return isOnline ? R.drawable.ic_device_dishwasher_online : R.drawable.ic_device_dishwasher_offline;
            } else if (type.contains("RICE_COOKER")) {
                return isOnline ? R.drawable.ic_device_rice_cooker_online : R.drawable.ic_device_rice_cooker_offline;
            } else if (type.contains("AIR_FRYER")) {
                return isOnline ? R.drawable.ic_device_air_fryer_online : R.drawable.ic_device_air_fryer_offline;
            } else if (type.contains("COFFEE_MAKER")) {
                return isOnline ? R.drawable.ic_device_coffee_maker_online : R.drawable.ic_device_coffee_maker_offline;
            } else if (type.contains("STERILIZER")) {
                return isOnline ? R.drawable.ic_device_sterilizer_online : R.drawable.ic_device_sterilizer_offline;
            }

            // 默认图标
            return R.drawable.ic_launcher_foreground;
        }
    }
}