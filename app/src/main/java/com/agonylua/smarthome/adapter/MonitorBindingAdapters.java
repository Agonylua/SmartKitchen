package com.agonylua.smarthome.adapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.model.DeviceMode;

import java.util.List;

public class MonitorBindingAdapters {

    /**
     * 控制视图的“呼吸灯”动画
     * 只要 isBreathing 为 true，动画就自动开启，在 XML 中一行代码搞定
     */
    @BindingAdapter("isBreathing")
    public static void setBreathingAnimation(View view, boolean isBreathing) {
        ObjectAnimator animator = (ObjectAnimator) view.getTag();

        if (isBreathing) {
            view.setVisibility(View.VISIBLE);
            if (animator == null) {
                animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.2f, 1f);
                animator.setDuration(1500);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                view.setTag(animator);
            }
            if (!animator.isRunning()) {
                animator.start();
            }
        } else {
            view.setVisibility(View.INVISIBLE);
            if (animator != null) {
                animator.cancel();
            }
        }
    }

    /**
     * 自动向 RecyclerView 提交数据列表
     */
    @BindingAdapter("runningDeviceList")
    public static void submitRunningDeviceList(RecyclerView recyclerView, List<Device> devices) {
        if (recyclerView.getAdapter() instanceof MonitorAdapter) {
            ((MonitorAdapter) recyclerView.getAdapter()).submitList(devices);
        }
    }

    /**
     * 将 Device 状态/模式转化为中文文本
     */
    @BindingAdapter("deviceModeText")
    public static void setDeviceModeText(TextView textView, String mode) {
        if (mode == null) {
            textView.setText("异常");
            return;
        }
        String currentMode = DeviceMode.toLabel(mode);
        textView.setText(currentMode);
    }

    /**
     * 模拟能耗转换 (未来可以替换为真实的 device.power)
     */
    @BindingAdapter("mockPowerText")
    public static void setMockPowerText(TextView textView, String type) {
        if (type == null) {
            textView.setText("0 W");
            return;
        }
        String powerStr = "500 W";
        textView.setText(powerStr);
    }
}