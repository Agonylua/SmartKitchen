package com.agonylua.smarthome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.entity.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.MonitorViewHolder> {

    private Context context;
    private List<Device> deviceList;
    private OnDeviceClickListener listener;

    public MonitorAdapter(Context context) {
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
    public MonitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 绑定之前写的 item_device_card.xml
        View view = LayoutInflater.from(context).inflate(R.layout.card_device, parent, false);
        return new MonitorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonitorViewHolder holder, int position) {
        Device device = deviceList.get(position);
        Map<String, Object> data = device.getDeviceData();

        // 设置通用信息
        holder.tvDeviceName.setText(device.getDeviceName());
        //holder.tvStatus.setText("状态未知");
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
    static class MonitorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceStatus, tvDeviceName, tvCurrentTask, tvRemainingTime;

        public MonitorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.monitor_device_name);
            tvDeviceStatus = itemView.findViewById(R.id.monitor_device_status);
            tvCurrentTask = itemView.findViewById(R.id.monitor_current_task);
            tvRemainingTime = itemView.findViewById(R.id.monitor_remaining_time);
        }
    }
}