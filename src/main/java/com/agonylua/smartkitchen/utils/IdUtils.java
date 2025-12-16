package com.agonylua.smartkitchen.utils;

public class IdUtils {
    // 生成6位随机字符 (大小写字母+数字)
    public static String generateShortId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    // 生成12位纯数字 SN
    public static String generateDeviceSn() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int num = (int) (Math.random() * 10);
            sb.append(num);
        }
        return sb.toString();
    }
}