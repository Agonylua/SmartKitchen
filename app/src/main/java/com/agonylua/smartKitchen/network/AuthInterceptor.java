package com.agonylua.smartKitchen.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // 获取原始API请求
        Request originalRequest = chain.request();

        // 读取 Token
        SharedPreferences sp = context.getSharedPreferences("SmartKitchenApp", Context.MODE_PRIVATE);
        String token = sp.getString("jwt_token", "");

        if (token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        // 添加 Authorization 头
        Request newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response response = chain.proceed(newRequest);

        if (response.code() == 401) {
            new Handler(Looper.getMainLooper()).post(() -> {
                context.sendBroadcast(new Intent("com.agonylua.smartKitchen.ACTION_LOGOUT"));
            });
        }

        return response;
    }
}