package com.agonylua.smarthome.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.dto.AutomationRuleDTO;
import com.agonylua.smarthome.network.ApiResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartRepository {

    public void saveRule(Context context, AutomationRuleDTO rule, SmartCallback callback) {
        RetrofitClient.getInstance(context).getApi().createRule(rule).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        callback.onSuccess();
                    } else {
                        callback.onError("规则保存失败，错误代码: " + apiResponse.getCode());
                    }
                } else {
                    callback.onError("规则保存失败，服务器返回错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("网络连接错误: " + t.getMessage());
            }
        });
    }

    public interface SmartCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

}
