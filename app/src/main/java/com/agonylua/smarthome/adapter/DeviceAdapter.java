package com.agonylua.smarthome.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.model.DeviceState;
import com.agonylua.smarthome.model.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private Context context;
    private List<Device> deviceList;
    private OnDeviceClickListener listener;
    private static final String TAG = "DeviceAdapter";

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

    /**
     * 绑定数据到 ViewHolder
     *
     * @param holder   ViewHolder 实例
     * @param position 位置
     */
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position); // 获取当前设备
        Map<String, String> data = device.getDeviceData(); // 获取设备详细数据

        // 设置通用信息
        holder.tvDeviceName.setText(device.getDeviceName());

        // 根据 deviceType 设置图标和特定状态文本
        DeviceType deviceType = DeviceType.fromName(device.getDeviceType());
        if (deviceType == null) {
            return;
        }
        if (!Objects.equals(device.getDeviceStatus(), DeviceState.OFFLINE.name())) {
            Log.d(TAG, "device info: " + device);
            holder.ivDeviceIcon.setImageResource(deviceType.getOnline());
        } else {
            holder.ivDeviceIcon.setImageResource(deviceType.getOffline());
        }
//        if ("FRIDGE".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.ic_device_refrigerator_online);
//        } else if ("RICE_COOKER".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.ic_device_rice_cooker_online);
//        } else if ("MICROWAVE".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.ic_device_microwave_online);
//        } else if ("DISHWASHER".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.ic_device_dishwasher_online);
//        } else if ("STERILIZER".equalsIgnoreCase(device.getDeviceType())) {
//            holder.ivDeviceIcon.setImageResource(R.drawable.ic_device_sterilizer_online);
//        }
        // TODO: 添加更多设备类型
        // 点击事件处理
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: " + device + "clicked: " + device.getDeviceType());
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