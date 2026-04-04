package com.agonylua.smartKitchen.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.dao.HomeDao;
import com.agonylua.smartKitchen.database.dao.RulesDao;
import com.agonylua.smartKitchen.database.entity.Home;
import com.agonylua.smartKitchen.dto.HomeDTO;
import com.agonylua.smartKitchen.dto.UserDTO;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserManager userManager;
    private RetrofitClient retrofit;
    private HomeDao homeDao;
    private DeviceDao deviceDao;
    private RulesDao rulesDao;

    @Inject
    public UserRepository(UserManager userManager, RetrofitClient retrofit, HomeDao homeDao, DeviceDao deviceDao, RulesDao rulesDao) {
        this.userManager = userManager;
        this.retrofit = retrofit;
        this.homeDao = homeDao;
        this.deviceDao = deviceDao;
        this.rulesDao = rulesDao;
    }

    public LiveData<Home> getHome() {
        return homeDao.getHomeByUserId(userManager.getUserId());
    }

    public void updateUserNickname(String newNickname, infoCallback callback) {
        retrofit.getApi().updateNickname(userManager.getUserId(), newNickname).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        userManager.setNickname(newNickname);
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

    public void updateUserAvatar(File newAvatarFile, infoCallback callback) {
        RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("UserAvatar"), newAvatarFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", newAvatarFile.getName(), requestFile);
        retrofit.getApi().updateAvatar(body, userManager.getUserId()).enqueue(new Callback<ApiResponse<String>>() {
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

    public void resetUserPassword(String oldPassword, String newPassword, infoCallback callback) {
        retrofit.getApi().resetPassword(userManager.getUserId(), oldPassword, newPassword).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
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

    public void getUserInfo(infoCallback callback) {
        // 1. 发起网络请求
        retrofit.getApi().getUserInfo().enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDTO>> call, @NonNull Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserDTO> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        userManager.saveUser(
                                apiResponse.getData().getNickname(),
                                apiResponse.getData().getAvatarUrl()
                        );
                        callback.onSuccess();
                    } else {
                        callback.onError(apiResponse.getMessage());
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

    public void getUserListInfo(usersInfoCallback callback) {
        // 1. 发起网络请求
        retrofit.getApi().getUserListInfo(userManager.getHomeId()).enqueue(new Callback<ApiResponse<List<UserDTO>>>() {
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

    public void joinHome(String homeId, joinCallback callback) {
        retrofit.getApi().joinHome(homeId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
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
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    public void exitHome(infoCallback callback) {
        String homeId = userManager.getHomeId();
        // 1. 发起网络请求
        retrofit.getApi().exitHome(homeId).enqueue(new Callback<ApiResponse<UserDTO>>() {
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
                        retrofit.getApi().getHomeInfo(body.getData().getHomeId()).enqueue(new Callback<ApiResponse<Home>>() {
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

    public void logout() {
        userManager.clear();
        ThreadPoolUtils.getInstance().execute(() -> {
            homeDao.clearAll();
            deviceDao.clearAll();
            rulesDao.clearAll();
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

    public interface joinCallback {
        void onSuccess(String refresh);

        void onError(String message);
    }
}
