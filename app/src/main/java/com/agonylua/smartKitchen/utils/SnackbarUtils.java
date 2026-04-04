package com.agonylua.smartKitchen.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

        // 1. 设置悬浮和外边距 (Floating 效果)
        ViewGroup.LayoutParams params = snackbarView.getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) params;
            flp.setMargins(48, 0, 48, 80); // 左右留白，底部抬高避开底部导航栏
            snackbarView.setLayoutParams(flp);
        }

        // 2. 动态生成胶囊圆角背景 (Pill shape)
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(100f); // 极大的圆角半径形成胶囊形

//        // 3. 根据类型配置颜色和图标
//        int backgroundColor = Color.parseColor("#323232"); // 默认深灰
//        int iconResId = R.drawable.ic_network; // 默认图标，可替换为 info icon
//
//        switch (type) {
//            case SUCCESS:
//                backgroundColor = Color.parseColor("#00897B"); // Teal 600
//                iconResId = R.drawable.ic_add; // 请替换为项目中实际的 success_icon，例如勾号
//                break;
//            case WARNING:
//                backgroundColor = Color.parseColor("#F57C00"); // Orange 700
//                iconResId = R.drawable.ic_settings; // 请替换为实际的 warning_icon
//                break;
//            case ERROR:
//                backgroundColor = Color.parseColor("#E53935"); // Red 600
//                iconResId = R.drawable.ic_left_arrow; // 请替换为实际的 error_icon
//                break;
//            case INFO:
//            default:
//                break;
//        }
//
//        background.setColor(backgroundColor);
        snackbarView.setBackground(background);
        snackbarView.setElevation(8f); // 增加阴影体现层次

        // 4. 配置文字和左侧图标
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(14f);
            textView.setMaxLines(2);
            // 设置左侧图标
//            textView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
//            textView.setCompoundDrawablePadding(24); // 图标与文字的间距
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