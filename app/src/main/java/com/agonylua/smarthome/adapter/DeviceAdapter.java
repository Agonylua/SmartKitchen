package com.agonylua.smarthome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.Model.Device;
import com.agonylua.smarthome.Model.DeviceData;
import com.agonylua.smarthome.R;

import java.util.ArrayList;
import java.util.List;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private Context context;
    private List<Device> deviceList;
    private OnDeviceClickListener listener;

    public DeviceAdapter(Context context) {
        this.context = context;
        this.deviceList = new ArrayList<>();
    }

    public void setDeviceList(List<Device> list) {
        this.deviceList = list;
        notifyDataSetChanged(); // 刷新列表
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 绑定之前写的 item_device_card.xml
        View view = LayoutInflater.from(context).inflate(R.layout.card_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        DeviceData data = device.getDeviceData();

        // 设置通用信息
        holder.tvDeviceName.setText(device.getDeviceName());

        // 根据 deviceType 设置图标和特定状态文本
//        if ("FRIDGE".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.refrigerator);
//            if (data != null) {
//                holder.tvStatus.setText("冷藏: " + data.getTemp() + "°C | 湿度: " + data.getHum() + "%");
//            }
//        } else if ("RICE_COOKER".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.rice_cooker);
//            holder.tvStatus.setText("状态: 空闲中");
//        } else {
//            // 未知设备类型
//            holder.tvStatus.setText("状态未知");
//        }
        // TODO: 添加更多设备类型
        // 3. 点击事件处理
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeviceClick(device);
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    // 定义点击事件接口，供 Fragment 调用
    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);

        void onSwitchClick(Device device, boolean isChecked);
    }

    // ViewHolder 类
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDeviceIcon;
        TextView tvDeviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeviceIcon = itemView.findViewById(R.id.iv_device_icon);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
        }
    }
}