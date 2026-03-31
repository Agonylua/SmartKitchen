package com.agonylua.smarthome.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.common.UserRequest;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.HomeDao;
import com.agonylua.smarthome.database.entity.Home;
import com.agonylua.smarthome.dto.HomeDTO;
import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.UserManager;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserManager userManager;
    private RetrofitClient retrofit;
    private HomeDao homeDao;

    public UserRepository(Application application) {
        this.userManager = UserManager.getInstance(application);
        retrofit = RetrofitClient.getInstance(application);
        homeDao = AppDatabase.getInstance(application).homeDao();
    }

    public LiveData<Home> getHome() {
        return homeDao.getHomeByUserId(userManager.getUserId());
    }

    /**
     * 更新用户信息
     *
     * @param request  包含要修改的字段
     * @param callback 回调给 ViewModel
     */
    public void updateUserInfo(UserRequest request, infoCallback callback) {
        // 1. 发起网络请求
        retrofit.getApi().updateUserInfo(request).enqueue(new Callback<ApiResponse<Void>>() {
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

    public void getUsersInfo(usersInfoCallback callback) {
        // 1. 发起网络请求
        retrofit.getApi().getUsersInfo(userManager.getHomeId()).enqueue(new Callback<ApiResponse<List<UserDTO>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<UserDTO>>> call, @NonNull Response<ApiResponse<List<UserDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserDTO>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<UserDTO>>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    public void uploadUserAvatar(MultipartBody.Part file, String userId, infoCallback callback) {
        // 1. 发起网络请求
        retrofit.getApi().uploadUserAvatar(file, userId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        userManager.setAvatarUrl(apiResponse.getData());
                        callback.onSuccess();
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    public void removeMember(String memberId, infoCallback callback) {
        String homeId = userManager.getHomeId();
        // 1. 发起网络请求
        retrofit.getApi().removeMember(homeId, memberId).enqueue(new Callback<ApiResponse<HomeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<HomeDTO>> call, @NonNull Response<ApiResponse<HomeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<HomeDTO> apiResponse = response.body();
                    Log.d(TAG, "onResponse: " + apiResponse.getCode() + apiResponse.getMessage());
                    if (apiResponse.getCode() == 200) {
                        ThreadPoolUtils.getInstance().execute(() -> {
                            homeDao.getHomeByHomeId(homeId).setMemberIds(apiResponse.getData().getMemberIds());
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
            public void onFailure(@NonNull Call<ApiResponse<HomeDTO>> call, @NonNull Throwable t) {

            }
        });
    }

    public void exitHome(infoCallback callback) {
        String homeId = userManager.getHomeId();
        String userId = userManager.getUserId();
        // 1. 发起网络请求
        retrofit.getApi().exitHome(homeId, userId).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserDTO> body = response.body();
                    if (body.getCode() == 200) {
                        userManager.clear();
                        userManager.saveUserInfo(
                                body.getData().getUserId(),
                                body.getData().getHomeId(),
                                body.getData().getUsername(),
                                body.getData().getNickname(),
                                body.getData().getAvatarUrl(),
                                body.getData().getToken()
                        );
                        callback.onSuccess();
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    // 回调接口：通知 ViewModel 结果
    public interface infoCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface usersInfoCallback {
        void onSuccess(List<UserDTO> userList);

        void onError(String message);
    }
}
