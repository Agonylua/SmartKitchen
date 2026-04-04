package com.agonylua.smartKitchen.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.agonylua.smartKitchen.utils.UserManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {

    private Context context;
    @Inject
    public UserManager userManager;

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
                userManager.triggerTokenExpired();
            });
        }

        return response;
    }
}