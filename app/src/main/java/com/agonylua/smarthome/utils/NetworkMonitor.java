package com.agonylua.smarthome.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.repository.GlobalRepository;

public class NetworkMonitor {
    private static NetworkMonitor instance;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static NetworkMonitor getInstance(Context context) {
        synchronized (NetworkMonitor.class) {
            if (instance == null) {
                instance = new NetworkMonitor(context);
            }
        }
        return instance;
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
            // 改用 Socket 连接检测，因为现代 Android 系统常限制 ping 命令权限
            // 尝试连接 阿里DNS (223.5.5.5) 的 53 端口 (DNS)，超时设为 2000ms
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("223.5.5.5", 53), 2000);
            socket.close();
            return true;
        } catch (Exception e) {
            Log.w("NetworkMonitor", "Internet reachability check failed: " + e.getMessage());
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