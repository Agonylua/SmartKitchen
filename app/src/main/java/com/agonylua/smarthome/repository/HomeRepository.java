package com.agonylua.smarthome.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.common.DeviceRequest;
import com.agonylua.smarthome.common.DeviceResponse;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.UserManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private final String TAG = "HomeRepository";
    private static volatile HomeRepository instance;
    private final DeviceDao deviceDao;
    private UserManager userManager;
    private RetrofitClient retrofit;

    private HomeRepository(Application application) {
        deviceDao = AppDatabase.getInstance(application).deviceDao();
        retrofit = RetrofitClient.getInstance(application);
        userManager = UserManager.getInstance(application);
    }

    public static HomeRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (HomeRepository.class) {
                if (instance == null) {
                    instance = new HomeRepository(application);
                }
            }
        }
        return instance;
    }

    public LiveData<List<Device>> getDeviceList(String homeId) {
        return deviceDao.getDevicesListByHomeId(homeId);
    }

    public void getDevices(Context context, String homeId, DeviceListCallback callback) {
        retrofit.getApi().getDeviceList(homeId).enqueue(new Callback<DeviceResponse<Device>>() {
            @Override
            public void onResponse(@NonNull Call<DeviceResponse<Device>> call, @NonNull Response<DeviceResponse<Device>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    DeviceResponse<Device> DeviceResponse = response.body();

                    if (DeviceResponse.getCode() == 200) {
                        Log.d(TAG, "onResponse: 设备列表获取成功，设备数量: " + DeviceResponse.getData().size());
                        new Thread(() -> {
                            deviceDao.clearAll();
                            deviceDao.insertAll(response.body().getData());
                        }).start();
                        callback.onSuccess(DeviceResponse.getData());
                    } else {
                        callback.onFailure("设备列表获取失败，错误代码: " + DeviceResponse.getCode());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceResponse<Device>> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: 网络连接错误: " + t.getMessage());
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void validateToken(Context context, String token, VerifyCallback callback) {
        Log.d(TAG, "validateToken: " + token);
        Call<Void> call = retrofit.getApi().validateToken();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "onResponse: " + response.code());
                // 处理服务器响应
                if (response.isSuccessful()) {
                    // HTTP 200 OK -> Token 有效
                    callback.onVerify(true);
                } else {
                    // HTTP 401/403 -> Token 过期或非法
                    callback.onVerify(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : "未知错误";
                Log.e(TAG, "onFailure: " + msg);
                callback.onFailure(msg);
            }
        });
    }

    public void deleteDevice(Context context, String deviceSn, DeleteDeviceCallback callback) {
        new Thread(() -> {
            deviceDao.deleteByDeviceSn(deviceSn);
        }).start();
        DeviceRequest request = new DeviceRequest(deviceSn, userManager.getHomeId(), userManager.getUserId());
        retrofit.getApi().unBindDevice(request).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Log.d(TAG, "onResponse: 设备删除成功");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "onResponse: 设备删除失败，错误代码: " + apiResponse.getCode());
                        callback.onFailure("设备删除失败，错误代码: " + apiResponse.getCode());
                        // TODO: 可选 - 重新拉取以恢复数据
                    }
                } else {
                    callback.onFailure("设备删除失败: 网络响应无效");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: 设备删除网络错误 " + t.getMessage());
                callback.onFailure("网络错误: " + t.getMessage());
            }
        });
    }

    public interface VerifyCallback {
        void onVerify(Boolean isValid);

        void onFailure(String errorMessage);
    }

    public interface DeviceListCallback {
        void onSuccess(List<Device> devices);

        void onFailure(String errorMessage);
    }

    public interface DeleteDeviceCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

}
