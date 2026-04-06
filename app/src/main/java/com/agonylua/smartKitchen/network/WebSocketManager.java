package com.agonylua.smartKitchen.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {
    private static final String BASE_URL = "ws://192.168.116.113:1234";
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    // 重连逻辑相关
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final int MAX_RECONNECT_DELAY = 30 * 1000; // 最大重连间隔 30 秒
    private OkHttpClient client;
    private WebSocket webSocket;
    private String currentUserId; // 缓存当前用户ID用于重连
    private OnNotificationListener notificationListener;
    private int reconnectCount = 0; // 当前重连次数
    private boolean isManualClose = false; // 是否是用户手动关闭（退出登录）

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS) // 每30秒发送一次心跳包
                .retryOnConnectionFailure(true)
                .build();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setNotificationListener(OnNotificationListener listener) {
        this.notificationListener = listener;
    }

    /**
     * 连接入口
     */
    public void connect(String userId) {
        if (userId == null) return;
        this.currentUserId = userId;
        this.isManualClose = false;

        // 如果已经连接，先不要重复连
        if (webSocket != null) return;

        // 替换为你的服务器地址
        String wsUrl = BASE_URL + "/ws/notification?userId=" + userId;

        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "🟢 WebSocket 连接成功!");
                reconnectCount = 0; // 连接成功，重置重连计数器
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                if (notificationListener != null) {
                    mainHandler.post(() -> notificationListener.onReceiveJoinRequest(text));
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "🟡 服务器正在关闭连接: " + reason);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "⚫ 连接已关闭");
                WebSocketManager.this.webSocket = null;
                // 如果不是手动关闭，则尝试重连
                tryReconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Log.e(TAG, "❌ WebSocket 连接失败: " + t.getMessage());
                WebSocketManager.this.webSocket = null;
                // 尝试重连
                tryReconnect();
            }
        });
    }

    /**
     * 指数退避重连逻辑
     */
    private void tryReconnect() {
        if (isManualClose || currentUserId == null) {
            return; // 如果用户手动登出，不重连
        }

        reconnectCount++;
        // 计算下一次重连的延迟时间：2^n 秒，最大不超过 30 秒
        long delay = (long) Math.min(Math.pow(2, reconnectCount) * 1000, MAX_RECONNECT_DELAY);

        Log.d(TAG, "⏳ 将在 " + (delay / 1000) + " 秒后尝试第 " + reconnectCount + " 次重连...");

        mainHandler.removeCallbacksAndMessages(null); // 清除之前的任务
        mainHandler.postDelayed(() -> {
            Log.d(TAG, "🚀 正在执行重连...");
            connect(currentUserId);
        }, delay);
    }

    /**
     * 手动断开
     */
    public void disconnect() {
        this.isManualClose = true;
        this.currentUserId = null;
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    public interface OnNotificationListener {
        void onReceiveJoinRequest(String jsonMessage);
    }
}