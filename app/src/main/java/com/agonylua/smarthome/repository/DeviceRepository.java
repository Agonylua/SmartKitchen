package com.agonylua.smarthome.repository;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.network.MqttManager;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.JsonUtils;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private static volatile DeviceRepository instance;
    private DeviceDao deviceDao;
    private LiveData<Device> device;


    private DeviceRepository(Application application) {
        deviceDao = AppDatabase.getInstance(application).deviceDao();
    }

    public static DeviceRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (DeviceRepository.class) {
                if (instance == null) {
                    instance = new DeviceRepository(application);
                }
            }
        }
        return instance;
    }

    public LiveData<Device> getDevice(String deviceSn) {
        return deviceDao.getDeviceDataBySn(deviceSn);
    }

    public void updateDevice(Device device) {
        ThreadPoolUtils.getInstance().execute(() -> {
            this.device = deviceDao.getDeviceDataBySn(device.getDeviceSn());
            deviceDao.update(device);
        });

    }

    public void mqttControlMessage(Map<String, String> payload, String deviceSn) {
        MqttManager.getInstance().publish(deviceSn, JsonUtils.toJson(payload));
    }

    public void sendControlCmd(Context context, Map<String, String> payload, callback callback) {

        //调用 Retrofit 发送控制命令到服务器进行验证
        RetrofitClient.getInstance(context).getApi().controlDevice(payload).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.body() != null && response.body().getCode() == 200) {
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

    public interface callback {
        void onSuccess(String message);

        void onFailure(String errorMessage);
    }
}
