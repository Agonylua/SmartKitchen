package com.agonylua.smarthome.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 全局统一时钟引擎 (解决多设备并发更新导致的性能和内存问题)
 */
public class GlobalTicker {

    private static GlobalTicker instance;
    // 发送当前系统时间戳 (毫秒)
    private final MutableLiveData<Long> tickLiveData = new MutableLiveData<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            tickLiveData.setValue(System.currentTimeMillis());
            // 严格保证 1000ms 执行一次
            handler.postDelayed(this, 1000);
        }
    };
    private boolean isRunning = false;

    private GlobalTicker() {
    }

    public static synchronized GlobalTicker getInstance() {
        if (instance == null) {
            instance = new GlobalTicker();
        }
        return instance;
    }

    /**
     * 获取全局时钟的 LiveData，供各 UI 页面观察
     */
    public LiveData<Long> getTickLiveData() {
        if (!isRunning) {
            start();
        }
        return tickLiveData;
    }

    private void start() {
        isRunning = true;
        handler.post(tickRunnable);
    }

    // 可在 App 退到后台时调用此方法暂停，节省电量 (可选)
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
    }
}