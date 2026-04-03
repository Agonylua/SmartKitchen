package com.agonylua.smarthome.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.common.DeviceRequest;
import com.agonylua.smarthome.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDeviceRepository {
    private static final String TAG = "AddDeviceRepository";
    private static volatile AddDeviceRepository instance;
    private RetrofitClient retrofitClient;

    private AddDeviceRepository(Application application) {
        retrofitClient = RetrofitClient.getInstance(application);
    }

    public static AddDeviceRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (AddDeviceRepository.class) {
                if (instance == null) {
                    instance = new AddDeviceRepository(application);
                }
            }
        }
        return instance;
    }

    public void bindDevice(String deviceSn, String homeId, callback callback) {
        DeviceRequest request = new DeviceRequest(deviceSn, homeId);
        Log.d(TAG, "bindDevice: " + deviceSn + " " + homeId);
        retrofitClient.getApi().bindDevice(request).enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.code() == 200 && response.body() != null) {
                    Integer data = response.body().getData();
                    if (data != null) {
                        callback.onSuccess(data);
                    } else {
                        callback.onFailure("Response data is null");
                    }
                } else {
                    callback.onFailure("Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public interface callback {
        void onSuccess(int code);

        void onFailure(String errorMessage);
    }
}