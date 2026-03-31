package com.agonylua.smartkitchen.utils;

import java.security.SecureRandom;

public class IdUtil {
    // 全局安全随机数实例，避免重复创建
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    // 大小写字母+数字字符集
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // 抽象通用纯数字生成方法（消除冗余）
    private static String generatePureNumber(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("数字长度必须大于0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 直接生成0-9的随机整数，比Math.random()更高效、更安全
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    // 生成6位随机字符 (纯数字)
    public static String generateUserId() {
        return generatePureNumber(6);
    }

    // 生成12位纯数字 SN
    public static String generateDeviceSn() {
        String numStr = generatePureNumber(12);
        return "SK-" + numStr;
    }

    // 生成6位随机字符 (大小写字母+数字)
    public static String generateHomeId() {
        int poolLength = CHAR_POOL.length();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            // 安全随机生成字符集索引
            int index = SECURE_RANDOM.nextInt(poolLength);
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    public static String generateRulesId() {
        return generateHomeId();
    }
}

