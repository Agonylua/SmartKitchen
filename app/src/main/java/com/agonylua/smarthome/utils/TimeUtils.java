package com.agonylua.smarthome.utils;

import java.util.Locale;

public class TimeUtils {
    /**
     * 将总秒数格式化为 dd:hh:mm 格式
     * 例如：1天2小时5分钟 -> 01:02:05
     */
    public static String formatToDdHhMm(long totalSeconds) {
        if (totalSeconds <= 0) return "--:--:--";

        long days = totalSeconds / 86400; // 24小时 * 3600秒
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        // 使用 %02d 保证不足两位时补 0
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", days, hours, minutes);
    }
}