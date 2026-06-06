package com.agonylua.smartKitchen.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkMonitor {
    private static NetworkMonitor instance;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Application application) {
        connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static synchronized NetworkMonitor getInstance(Application application) {
            if (instance == null) {
                instance = new NetworkMonitor(application);
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
                Log.d("NetworkMonitor", "网络已连接");
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // 网络已断开
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

    // 检查当前是否真正能访问互联网（同步方法，可在主线程直接调用）
    public boolean isInternetReachable() {
        if (connectivityManager == null) return false;

        // 针对 Android 6.0 (API 23) 及以上版本的处理
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) return false;

        // 1. 基础条件：必须具备连接互联网的能力
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return false;
        }

        // 2. 完美情况：系统已经验证连通了外网
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true;
        }

        // 3. 明确拦截情况：系统检测到这是一个需要登录认证的公共网络（Captive Portal）
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
            return false;
        }

        // 4. 薛定谔状态：没有被系统 Validated，但也没有明确是 Portal。
        // 核心优化点：为了避免“明明有网却返回false”（如刚连上WiFi的延迟、或者系统验证服务器被墙），
        // 此时我们选择放行（返回 true）。如果真实网络确实不通，交由 OkHttp/Retrofit 的 IOException 来兜底。
        return true;

    }

    // 检查当前是否有网络连接（只检查是否连上了路由器/基站，不代表一定能上网）
    public boolean isNetworkConnected() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // 停止监听 (防止内存泄漏)
    public void stopMonitoring() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

}