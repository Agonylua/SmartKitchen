package com.agonylua.smarthome.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.common.DeviceResponse;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.dto.DevicePowerDTO;
import com.agonylua.smarthome.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitorRepository {
    private static final String TAG = "MonitorRepository";
    private static volatile MonitorRepository instance;
    private DeviceDao deviceDao;
    private RetrofitClient retrofit;

    private MonitorRepository(Application application) {
        deviceDao = AppDatabase.getInstance(application).deviceDao();
        retrofit = RetrofitClient.getInstance(application);
    }

    public static MonitorRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (MonitorRepository.class) {
                if (instance == null) {
                    instance = new MonitorRepository(application);
                }
            }
        }
        return instance;
    }

    public void getDevicePowerData(String homeId, Context context, poserCallback poserCallback) {
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
                        callback.onSuccess();
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
