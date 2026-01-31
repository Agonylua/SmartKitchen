package com.agonylua.smarthome.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.network.DeviceResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private final String TAG = "HomeRepository";
    private final DeviceDao deviceDao;

    public HomeRepository(Context context) {
        deviceDao = AppDatabase.getInstance(context).deviceDao();
    }

    public LiveData<List<Device>> getDeviceList(String homeId) {
        return deviceDao.getDevicesByHomeId(homeId);
    }

    public void getDevices(Context context, String homeId, DeviceListCallback callback) {
        RetrofitClient.getInstance(context).getApi().getDeviceList(homeId).enqueue(new Callback<DeviceResponse<Device>>() {
            @Override
            public void onResponse(@NonNull Call<DeviceResponse<Device>> call, @NonNull Response<DeviceResponse<Device>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    DeviceResponse<Device> DeviceResponse = response.body();

                    if (DeviceResponse.getCode() == 200) {
                        Log.d(TAG, "onResponse: 设备列表获取成功，设备数量: " + DeviceResponse.getData().size());
                        new Thread(() -> {
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

        Call<Void> call = RetrofitClient.getInstance(context).getApi().validateToken(token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
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
    public interface VerifyCallback {
        void onVerify(Boolean isValid);

        void onFailure(String errorMessage);
    }

    public interface DeviceListCallback {
        void onSuccess(List<Device> devices);

        void onFailure(String errorMessage);
    }
}
