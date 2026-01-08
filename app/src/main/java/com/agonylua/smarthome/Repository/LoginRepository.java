package com.agonylua.smarthome.Repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.DTO.UserDTO;
import com.agonylua.smarthome.Model.LoginRequest;
import com.agonylua.smarthome.Model.LoginResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// LoginRepository.java
public class LoginRepository {
    private final String TAG = "callback";

    public void login(Context context, String username, String password, final LoginCallback callback) {

        LoginRequest request = new LoginRequest(username, password);

        Call<LoginResponse<UserDTO>> call = RetrofitClient.getInstance(context).getApi().login(request);

        call.enqueue(new Callback<LoginResponse<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse<UserDTO>> call, @NonNull Response<LoginResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse<UserDTO> body = response.body();

                    if (body.getData() != null && body.getData().getToken() != null) {
                        // 回调成功
                        callback.onSuccess(body.getData().getToken());
                        Log.i(TAG, "登陆成功信息: " + body.getData().getToken());
                    } else {
                        // 数据为空或Token为空，视为失败
                        callback.onError("登录失败，服务器响应异常");
                        Log.i(TAG, "登陆失败，返回数据不完整");
                    }
                } else {
                    // HTTP 请求成功但业务失败 (如 404, 500)
                    callback.onError("登录失败，请检查账号密码");
                    Log.i(TAG, "登录请求被拒绝: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse<UserDTO>> call, @NonNull Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : "未知错误";
                callback.onError("网络错误: " + msg);
                Log.e(TAG, "onFailure: " + msg);
            }
        });
    }

    public interface LoginCallback {
        void onSuccess(String token);

        void onError(String message);
    }
}