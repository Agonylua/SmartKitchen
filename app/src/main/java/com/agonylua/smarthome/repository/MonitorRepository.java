package com.agonylua.smarthome.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.dto.DevicePowerDTO;
import com.agonylua.smarthome.network.ApiResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitorRepository {

    private DeviceDao deviceDao;

    public MonitorRepository(Context context) {
        deviceDao = AppDatabase.getInstance(context).deviceDao();
    }

    public void getDevicePowerData(String homeId, Context context, callback callback) {
        RetrofitClient.getInstance(context).getApi().getDevicesPower(homeId).enqueue(new Callback<ApiResponse<List<DevicePowerDTO>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<DevicePowerDTO>>> call, @NonNull Response<ApiResponse<List<DevicePowerDTO>>> response) {
                if (response.body() != null && response.body().getCode() == 200) {
                    List<DevicePowerDTO> data = response.body().getData();
                    callback.onSuccess(data);
                } else {
                    callback.onFailure("Failed to fetch device power data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<DevicePowerDTO>>> call, @NonNull Throwable t) {

            }
        });
    }

    public LiveData<List<Device>> getOnlineDevices() {
        return deviceDao.getOnlineDevices();
    }

    public LiveData<Integer> getOnlineCount() {
        return deviceDao.getOnlineCount();
    }

    public interface callback {
        void onSuccess(List<DevicePowerDTO> data);

        void onFailure(String error);
    }
}
