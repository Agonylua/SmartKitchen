package com.agonylua.smarthome.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 网络检测工具类
 * 适配 Android 10+ (API 29)
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static NetworkUtils instance;

    /**
     * 判断网络是否连接 (基础检查)
     * 注意：这只能判断是否连接到了路由器/基站，不能判断是否真的能上网 (比如路由器欠费)
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        // Android M (6.0) 及以上使用 NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return false;

            // 只要具备 INTERNET 能力即视为已连接
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            // 旧版本兼容 (API < 23)
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /**
     * 判断是否是 Wi-Fi 连接
     * 场景：智能家居配网时，必须要求用户连接 Wi-Fi
     */
    public static boolean isWifiConnected(Context context) {
        return checkNetworkType(context, NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * 判断是否是移动数据 (4G/5G)
     * 场景：提醒用户正在消耗流量查看监控视频
     */
    public static boolean isMobileData(Context context) {
        return checkNetworkType(context, NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    /**
     * 内部通用检查方法
     */
    private static boolean checkNetworkType(Context context, int transportType) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(transportType);
        } else {
            // 旧版本逻辑 (简略)
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected() && info.getType() == (transportType == NetworkCapabilities.TRANSPORT_WIFI ? ConnectivityManager.TYPE_WIFI : ConnectivityManager.TYPE_MOBILE);
        }
    }

    /**
     * 打开系统网络设置页
     * 场景：检测到无网时，跳转让用户去设置
     */
    public static void openWirelessSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 【高级】Ping 检测 (必须在子线程调用)
     * 场景：解决“连上Wifi但由于欠费无法上网”的假死状态
     * 结合之前写的 ThreadPoolUtils 使用
     *
     * @return true 表示真的能访问互联网
     */
    public static boolean isInternetRealAvailable() {
        try {
            // 尝试连接百度或谷歌的 DNS (114.114.114.114 或 8.8.8.8)
            // timeout 设置为 2 秒
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("114.114.114.114", 53), 2000);
            socket.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Ping failed: " + e.getMessage());
            return false;
        }
    }
}