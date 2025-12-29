package com.agonylua.smarthome.Repository;

import com.agonylua.smarthome.network.LoginRequest;
import com.agonylua.smarthome.network.LoginResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// LoginRepository.java
public class LoginRepository {

    // 2. 方法不再接收 LiveData，而是接收 Callback
    public void login(String username, String password, final LoginCallback callback) {

        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getInstance().getApi().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 回调成功
                    callback.onSuccess(response.body().getToken());
                } else {
                    // 回调失败
                    callback.onError("登录失败，请检查账号密码");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // 回调错误
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    // 1. 定义一个简单的回调接口
    public interface LoginCallback {
        void onSuccess(String token);

        void onError(String message);
    }
}