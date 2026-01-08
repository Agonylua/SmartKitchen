package com.agonylua.smarthome.Repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.Model.Device;
import com.agonylua.smarthome.Model.DeviceResponse;
import com.agonylua.smarthome.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private String TAG = "HomeRepository";

    public void getDevices(Context context, String homeId, DeviceCallback callback) {
        RetrofitClient.getInstance(context).getApi().getDeviceList(homeId).enqueue(new Callback<DeviceResponse<Device>>() {
            @Override
            public void onResponse(@NonNull Call<DeviceResponse<Device>> call, @NonNull Response<DeviceResponse<Device>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    DeviceResponse<Device> DeviceResponse = response.body();

                    if (DeviceResponse.getCode() == 200) {
                        Log.d(TAG, "onResponse: 设备列表获取成功，设备数量: " + DeviceResponse.getData().size());
                        callback.onSuccess(DeviceResponse.getData());
                    } else {
                        Log.e(TAG, "onResponse: 设备列表获取失败，错误信息: " + DeviceResponse.getMessage());
                        callback.onError(DeviceResponse.getMessage());
                    }
                } else {
                    Log.d(TAG, "onResponse: 请求失败，服务器状态码: " + response.code());
                    callback.onError("请求失败，服务器状态码: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceResponse<Device>> call, @NonNull Throwable t) {
                callback.onError("网络连接错误: " + t.getMessage());
                Log.d(TAG, "onFailure: 网络连接错误: " + t.getMessage());
            }
        });
    }

    public interface DeviceCallback {
        void onSuccess(List<Device> devices);

        void onError(String message);
    }
}
