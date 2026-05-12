package com.agonylua.smartKitchen.network;

import android.content.Context;

import com.agonylua.smartKitchen.service.ApiService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static String IP_PORT = "47.238.79.228:1234";
    private static RetrofitClient instance;
    private ApiService apiService;
    private Context context;

    private RetrofitClient(Context context) {
        this.context = context.getApplicationContext();
        buildApiService();
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.buildApiService();
        }
    }

    // 单例方法
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    private void buildApiService() {
        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        // 创建 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + IP_PORT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public ApiService getApi() {
        return apiService;
    }
}
