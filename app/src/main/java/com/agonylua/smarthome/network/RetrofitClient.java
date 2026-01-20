package com.agonylua.smarthome.network;

import android.content.Context;

import com.agonylua.smarthome.service.ApiService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.2.2.11:1234";
    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient(Context context) {
        // 1. 创建 OkHttpClient 并添加拦截器
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        // 2. 创建 Retrofit，把 OkHttp 塞进去
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // <--- 关键步骤
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // 修改单例方法，需要传入 Context
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}
