package com.agonylua.smarthome.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.network.ApiResponse;
import com.agonylua.smarthome.network.LoginRequest;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// LoginRepository.java
public class LoginRepository {
    private final String TAG = "callback";
    private UserManager userManager;

    public void login(Context context, String username, String password, final LoginCallback callback) {

        LoginRequest request = new LoginRequest(username, password);
        userManager = UserManager.getInstance(context);

        Call<ApiResponse<UserDTO>> call = RetrofitClient.getInstance(context).getApi().login(request);

        call.enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserDTO> body = response.body();

                    if (body.getData() != null && body.getData().getToken() != null) {
                        // 回调成功
                        userManager.saveLoginInfo(
                                body.getData().getUserId(),
                                body.getData().getHomeId(),
                                body.getData().getUsername(),
                                body.getData().getNickname()
                        );
                        callback.onSuccess(body.getData().getToken());
                        Log.i(TAG, "登陆成功信息: " + body.getData());
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
            public void onFailure(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Throwable t) {
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