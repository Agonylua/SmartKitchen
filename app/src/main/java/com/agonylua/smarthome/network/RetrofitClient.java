package com.agonylua.smarthome.network;

import com.agonylua.smarthome.Service.ApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 请替换为你电脑的局域网 IP，模拟器中 localhost 无法访问电脑
    private static final String BASE_URL = "http://10.1.1.5:8080/";
    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * 单例模式
     *
     * @return RetrofitClient 实例
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}
