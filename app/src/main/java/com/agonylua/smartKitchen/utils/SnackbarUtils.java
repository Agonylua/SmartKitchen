package com.agonylua.smartKitchen.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    /**
     * 显示全局统一样式的悬浮胶囊 Snackbar
     *
     * @param view    当前 Fragment/Activity 的 View (通常使用 requireView())
     * @param message 提示信息
     */
    public static void show(@NonNull View view, @NonNull String message) {
        Context context = view.getContext();
        if (context == null) return;

        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        // 设置宽度自适应、底部居中、悬浮边距
        ViewGroup.LayoutParams params = snackbarView.getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) params;
            // 宽度改为根据内容自适应
            flp.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            flp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            // 设置对齐方式为水平居中 + 底部对齐
            flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            // 底部抬高 80 避开导航栏；左右保留 48 防止极端情况下长文本贴边
            flp.setMargins(48, 0, 48, 160);
            snackbarView.setLayoutParams(flp);
        }

        // 2. 动态生成胶囊圆角背景 (保持不变)
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(100f);
//
//        // 3. 根据类型配置颜色和图标 (保持不变)
//        int backgroundColor = Color.parseColor("#323232");
//        int iconResId = R.drawable.ic_network;
//
//        switch (type) {
//            case SUCCESS:
//                backgroundColor = Color.parseColor("#00897B");
//                iconResId = R.drawable.ic_add;
//                break;
//            case WARNING:
//                backgroundColor = Color.parseColor("#F57C00");
//                iconResId = R.drawable.ic_settings;
//                break;
//            case ERROR:
//                backgroundColor = Color.parseColor("#E53935");
//                iconResId = R.drawable.ic_left_arrow;
//                break;
//            case INFO:
//            default:
//                break;
//        }
//
//        background.setColor(backgroundColor);
        snackbarView.setBackground(background);
        snackbarView.setElevation(8f);

        // 4. 配置文字和图标
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(14f);
            textView.setMaxLines(2);
            // 确保文本居中显示，让自适应宽度的胶囊看起来更匀称
//            textView.setGravity(Gravity.CENTER_VERTICAL);
//            textView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
//            textView.setCompoundDrawablePadding(24);
        }

        snackbar.show();
    }

    public enum Type {
        INFO,       // 常规提示 (蓝色/灰色)
        SUCCESS,    // 成功 (Teal/绿色，契合厨房主题)
        WARNING,    // 警告 (橙色，如设备离线)
        ERROR       // 错误 (红色，如指令发送失败)
    }
}