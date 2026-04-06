package com.agonylua.smartKitchen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smartKitchen.R;
import com.agonylua.smartKitchen.database.entity.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private static final String TAG = "DeviceAdapter";
    private List<Device> deviceList = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private String currentDeletingDeviceSn = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void submitList(List<Device> newDevices) {
        this.deviceList = newDevices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device, listener, longClickListener, currentDeletingDeviceSn, this);
    }

    @Override
    public int getItemCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    public void clearDeleteMode() {
        if (currentDeletingDeviceSn != null) {
            String tempId = currentDeletingDeviceSn;
            currentDeletingDeviceSn = null;
            for (int i = 0; i < deviceList.size(); i++) {
                String dSn = deviceList.get(i).getDeviceSn();
                if (dSn != null && dSn.equals(tempId)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public String getCurrentDeletingDeviceSn() {
        return currentDeletingDeviceSn;
    }

    public void setCurrentDeletingDeviceSn(String sn) {
        String oldSn = currentDeletingDeviceSn;
        currentDeletingDeviceSn = sn;
        if (oldSn != null && !oldSn.equals(currentDeletingDeviceSn)) {
            for (int i = 0; i < deviceList.size(); i++) {
                String dSn = deviceList.get(i).getDeviceSn();
                if (dSn != null && dSn.equals(oldSn)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    // 定义点击事件接口，方便在 Fragment 中处理页面跳转
    public interface OnItemClickListener {
        void onItemClick(Device device);
    }

    // 定义长按事件接口，用于删除设备等操作
    public interface OnItemLongClickListener {
        void onItemLongClick(Device device);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDeviceIcon;
        TextView tvDeviceName;
        View clDeleteOverlay;
        View btnDeleteConfirm;
        Runnable longPressRunnable;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeviceIcon = itemView.findViewById(R.id.iv_device_icon);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            clDeleteOverlay = itemView.findViewById(R.id.cl_delete_overlay);
            btnDeleteConfirm = itemView.findViewById(R.id.btn_delete_confirm);
        }

        public void bind(Device device, OnItemClickListener listener, OnItemLongClickListener longClickListener, String currentDeletingSn, DeviceAdapter adapter) {
            // 设置设备名称
            tvDeviceName.setText(device.getDeviceName() != null ? device.getDeviceName() : "未知设备");

            // 判断设备是否在线
            boolean isOnline = device.getDeviceStatus() != null && device.getDeviceStatus().equals("ONLINE");
            String deviceType = device.getDeviceType() != null ? device.getDeviceType() : "";

            // 根据设备类型和在线状态加载不同的图标
            ivDeviceIcon.setImageResource(getIconForDevice(deviceType, isOnline));

            boolean isDeleting = device.getDeviceSn() != null && device.getDeviceSn().equals(currentDeletingSn);
            clDeleteOverlay.setVisibility(isDeleting ? View.VISIBLE : View.GONE);

            // 自定义长按逻辑
            itemView.setOnLongClickListener(v -> {
                if (adapter.getCurrentDeletingDeviceSn() != null && !device.getDeviceSn().equals(adapter.getCurrentDeletingDeviceSn())) {
                    adapter.clearDeleteMode();
                }
                adapter.setCurrentDeletingDeviceSn(device.getDeviceSn());
                adapter.notifyItemChanged(getAdapterPosition());
                return true; // 返回 true 表示消耗了长按事件，不再触发点击
            });

            // 遮罩存在拦截点击，使得点击卡片其他地方取消删除
            clDeleteOverlay.setOnClickListener(v -> {
                adapter.setCurrentDeletingDeviceSn(null);
                adapter.notifyItemChanged(getAdapterPosition());
            });

            // 点击确认删除按钮
            btnDeleteConfirm.setOnClickListener(v -> {
                adapter.setCurrentDeletingDeviceSn(null);
                adapter.notifyItemChanged(getAdapterPosition());
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(device);
                }
            });

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