package com.agonylua.smarthome.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.common.ApiResponse;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.network.RetrofitClient;
import com.agonylua.smarthome.utils.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainRepository {
    private static volatile MainRepository instance;
    public LiveData<Device> newDeviceData = new MutableLiveData<>();
    private DeviceDao deviceDao;
    private RetrofitClient retrofit;
    private UserManager userManager;
    private Device device;

    private MainRepository(Application application) {
        deviceDao = AppDatabase.getInstance(application).deviceDao();
        retrofit = RetrofitClient.getInstance(application);
        userManager = UserManager.getInstance(application);
    }

    public static MainRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (MainRepository.class) {
                if (instance == null) {
                    instance = new MainRepository(application);
                }
            }
        }
        return instance;
    }

    public LiveData<Device> getNewDeviceData() {
        return newDeviceData;
    }

    public void updateDeviceData(String sn, String newData) {
        deviceDao.updateDeviceData(sn, newData);
    }


    public void joinHomeApproval(boolean result, String memberId, MainRepository.joinCallback callback) {
        retrofit.getApi().joinHomeApproval(result, userManager.getUserId(), memberId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("服务器错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                callback.onError("网络连接失败: " + t.getMessage());
            }
        });
    }

    public interface joinCallback {
        void onSuccess(String refresh);

        void onError(String message);
    }
}
