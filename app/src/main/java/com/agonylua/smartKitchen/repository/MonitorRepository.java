package com.agonylua.smartKitchen.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.dto.DevicePowerDTO;
import com.agonylua.smartKitchen.network.RetrofitClient;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class MonitorRepository {
    private static final String TAG = "MonitorRepository";
    private DeviceDao deviceDao;
    private RetrofitClient retrofit;

    @Inject
    public MonitorRepository(RetrofitClient retrofit, DeviceDao deviceDao) {
        this.retrofit = retrofit;
        this.deviceDao = deviceDao;
    }

    public void getDevicePowerData(String homeId, poserCallback poserCallback) {
        retrofit.getApi().getDevicesPower(homeId).enqueue(new Callback<ApiResponse<List<DevicePowerDTO>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<DevicePowerDTO>>> call, @NonNull Response<ApiResponse<List<DevicePowerDTO>>> response) {
                if (response.body() != null && response.body().getCode() == 200) {
                    List<DevicePowerDTO> data = response.body().getData();
                    poserCallback.onSuccess(data);
                } else {
                    poserCallback.onFailure("Failed to fetch device power data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<DevicePowerDTO>>> call, @NonNull Throwable t) {

            }
        });
    }

    public void getDevices(String homeId, MonitorRepository.DeviceListCallback callback) {
        retrofit.getApi().getDeviceList(homeId).enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Device>>> call, @NonNull Response<ApiResponse<List<Device>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Device>> DeviceResponse = response.body();

                    if (DeviceResponse.getCode() == 200) {
                        Log.d(TAG, "onResponse: 设备列表获取成功，设备数量: " + DeviceResponse.getData().size());
                        new Thread(() -> {
                            deviceDao.clearAll();
                            deviceDao.insertAll(response.body().getData());
                        }).start();
                        callback.onSuccess();
                    } else {
                        callback.onFailure("设备列表获取失败，错误代码: " + DeviceResponse.getCode());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Device>>> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: 网络连接错误: " + t.getMessage());
                callback.onFailure(t.getMessage());
            }
        });
    }

    public LiveData<List<Device>> getOnlineDevices() {
        return deviceDao.getOnlineDevices();
    }

    public LiveData<Integer> getOnlineCount() {
        return deviceDao.getOnlineCount();
    }

    public interface poserCallback {
        void onSuccess(List<DevicePowerDTO> data);

        void onFailure(String error);
    }

    public interface DeviceListCallback {
        void onSuccess();

        void onFailure(String error);
    }
}
