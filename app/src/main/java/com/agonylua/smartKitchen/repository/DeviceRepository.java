package com.agonylua.smartKitchen.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.network.MqttManager;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.JsonUtils;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class DeviceRepository {
    private DeviceDao deviceDao;
    private RetrofitClient retrofitClient;
    private MqttManager mqttManager;

    @Inject
    public DeviceRepository(DeviceDao deviceDao, RetrofitClient retrofitClient, MqttManager mqttManager) {
        this.deviceDao = deviceDao;
        this.retrofitClient = retrofitClient;
        this.mqttManager = mqttManager;
    }

    public LiveData<Device> getDevice(String deviceSn) {
        return deviceDao.getDeviceDataBySn(deviceSn);
    }

    public void updateDevice(Device device) {
        ThreadPoolUtils.getInstance().execute(() -> {
            deviceDao.update(device);
        });

    }

    public void mqttControlMessage(Map<String, String> payload, String deviceSn) {
        mqttManager.publish(deviceSn, JsonUtils.toJson(payload));
    }

    public void sendControlCmd(Map<String, String> payload, callback callback) {

        //调用 Retrofit 发送控制命令到服务器进行验证
        retrofitClient.getApi().controlDevice(payload).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.code() == 200) {
                    callback.onSuccess("Control command sent successfully");
                } else {
                    callback.onFailure("error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void updateDeviceStatus(String deviceSn, callback callback) {

        retrofitClient.getApi().updateDeviceStatus(deviceSn).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.body() != null && response.body().getCode() == 200) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("连接错误");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                callback.onFailure("网络错误");
            }
        });
    }

    public void resetDeviceMode(String deviceSn) {
        ThreadPoolUtils.getInstance().execute(() -> {
            deviceDao.resetMode(deviceSn);
        });
    }

    public interface callback {
        void onSuccess(String message);

        void onFailure(String errorMessage);
    }
}
