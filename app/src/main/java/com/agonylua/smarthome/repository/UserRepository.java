package com.agonylua.smarthome.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.model.ApiResponse;
import com.agonylua.smarthome.model.UserRequest;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UserManager userManager;

    public UserRepository(Context context) {
        this.userManager = UserManager.getInstance(context);
    }

    /**
     * 更新用户信息
     *
     * @param request  包含要修改的字段
     * @param callback 回调给 ViewModel
     */
    public void updateUserInfo(UserRequest request, UpdateCallback callback) {
        // 1. 发起网络请求
        RetrofitClient.getInstance(null).getApi().updateUserInfo(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.getCode() == 200) {
                        updateLocalCache(request);
                        callback.onSuccess();
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    // 同步更新本地缓存
    private void updateLocalCache(UserRequest request) {
        userManager.saveUser(request.getNickname(), request.getAvatarUrl(), request.getHomeId());
    }

    // 回调接口：通知 ViewModel 结果
    public interface UpdateCallback {
        void onSuccess();

        void onError(String message);
    }
}
