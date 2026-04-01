package com.agonylua.smarthome.network;

import android.content.Context;

import com.agonylua.smarthome.service.ApiService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.89.85:1234";
    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient(Context context) {
        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        // 创建 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // 单例方法
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
