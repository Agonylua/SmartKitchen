package com.agonylua.smartKitchen.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.common.DeviceRequest;
import com.agonylua.smartKitchen.network.RetrofitClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AddDeviceRepository {
    private static final String TAG = "AddDeviceRepository";
    private RetrofitClient retrofit;

    @Inject
    public AddDeviceRepository(RetrofitClient retrofit) {
        this.retrofit = retrofit;
    }


    public void bindDevice(String deviceSn, String homeId, callback callback) {
        DeviceRequest request = new DeviceRequest(deviceSn, homeId);
        Log.d(TAG, "bindDevice: " + deviceSn + " " + homeId);
        retrofit.getApi().bindDevice(request).enqueue(new Callback<ApiResponse<Integer>>() {
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