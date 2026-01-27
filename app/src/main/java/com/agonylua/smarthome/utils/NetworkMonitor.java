package com.agonylua.smarthome.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.repository.GlobalRepository;

public class NetworkMonitor {

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    // 开启监听
    public void startMonitoring() {
        if (connectivityManager == null) return;

        // 定义回调
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                // 网络已连接
                GlobalRepository.getInstance().updateTheme(true);
                Log.d("NetworkMonitor", "网络已连接");
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // 网络已断开
                GlobalRepository.getInstance().updateTheme(false);
                Log.d("NetworkMonitor", "网络已断开");
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                // 网络能力变化（例如：从无弱信号变为强信号，或从受限网络变为验证网络）
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                }
            }
        };

        // 注册回调
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    // 手动检查互联网连接
    public boolean isInternetReachable() {
        try {
            // 尝试连接 Google DNS 或 百度/国内可靠域名
            // 这里的 1500 是超时时间 (毫秒)
            String ipAddr = "8.8.8.8"; // 或者 www.baidu.com
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ipAddr);
            int returnVal = p.waitFor();
            return (returnVal == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 停止监听 (防止内存泄漏)
    public void stopMonitoring() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

}