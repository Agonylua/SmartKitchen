package com.agonylua.smartKitchen.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.common.LoginRequest;
import com.agonylua.smartKitchen.database.dao.HomeDao;
import com.agonylua.smartKitchen.database.entity.Home;
import com.agonylua.smartKitchen.dto.UserDTO;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.NetworkMonitor;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class LoginRepository {
    private static volatile LoginRepository instance;
    private final String TAG = "poserCallback";
    private NetworkMonitor networkMonitor;
    private RetrofitClient retrofit;
    private UserManager userManager;
    private HomeDao homeDao;

    @Inject
    public LoginRepository(RetrofitClient retrofit, HomeDao homeDao, UserManager userManager, NetworkMonitor networkMonitor) {
        this.retrofit = retrofit;
        this.homeDao = homeDao;
        this.userManager = userManager;
        this.networkMonitor = networkMonitor;
    }

    public void login(String username, String password, final LoginCallback callback) {

        LoginRequest request = new LoginRequest(username, password);
        retrofit.getApi().login(request).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserDTO> body = response.body();

                    if (body.getData() != null && body.getData().getToken() != null) {
                        // 回调成功
                        userManager.saveUserInfo(
                                body.getData().getUserId(),
                                body.getData().getHomeId(),
                                body.getData().getUsername(),
                                body.getData().getNickname(),
                                body.getData().getAvatarUrl(),
                                body.getData().getToken()
                        );
                        Log.d(TAG, "onResponse: 用户信息保存成功，AvatarUrl: " + body.getData().getAvatarUrl());
                        callback.onSuccess();
                        Log.i(TAG, "登陆成功信息: " + body.getData());
                        getHomeInfo(body.getData().getHomeId(), new LoginCallback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "家庭信息获取失败: " + message);
                            }
                        });
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
                callback.onError("服务器连接错误");
                Log.e(TAG, "onFailure: " + msg);
            }
        });
    }

    public void tokenValidate(final ValidateCallback callback) {
        if (!userManager.isLogIn()) {
            callback.onVerify(false);
            return;
        }
        retrofit.getApi().validateToken().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "onResponse: " + response.code());
                // 处理服务器响应
                if (response.isSuccessful()) {
                    // HTTP 200 OK -> Token 有效
                    callback.onVerify(true);
                    getHomeInfo(userManager.getHomeId(), new LoginCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: " + "家庭信息获取成功");
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "家庭信息获取失败: " + message);
                        }
                    });
                } else {
                    // HTTP 401/403 -> Token 过期或非法
                    callback.onVerify(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : "未知错误";
                Log.e(TAG, "onFailure: " + msg);
                callback.onFailure("服务器连接失败");
            }
        });
    }

    public void getHomeInfo(String homeId, LoginCallback callback) {
        Log.d(TAG, "getHomeInfo: 请求家庭信息，homeId=" + homeId);
        retrofit.getApi().getHomeInfo(homeId).enqueue(new Callback<ApiResponse<Home>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Home>> call, @NonNull Response<ApiResponse<Home>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Home> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        ThreadPoolUtils.getInstance().execute(() -> {
                            homeDao.clearAll();
                            homeDao.insertAll(apiResponse.getData());
                        });
                        callback.onSuccess();
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Home>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    public boolean validateNetwork() {
        return networkMonitor.isInternetReachable();
    }

    public Boolean isExistToken() {
        return userManager.isLogIn();
    }

    public interface LoginCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface ValidateCallback {
        void onVerify(boolean isValid);

        void onFailure(String message);
    }
}