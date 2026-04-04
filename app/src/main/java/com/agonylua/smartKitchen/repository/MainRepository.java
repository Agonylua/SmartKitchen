package com.agonylua.smartKitchen.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.UserManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class MainRepository {
    private static volatile MainRepository instance;
    public LiveData<Device> newDeviceData = new MutableLiveData<>();
    private DeviceDao deviceDao;
    private RetrofitClient retrofit;
    private UserManager userManager;
    private Device device;

    @Inject
    public MainRepository(RetrofitClient retrofit, UserManager userManager, DeviceDao deviceDao) {
        this.retrofit = retrofit;
        this.userManager = userManager;
        this.deviceDao = deviceDao;
    }


    public LiveData<Device> getNewDeviceData() {
        return newDeviceData;
    }

    public void updateDeviceData(String sn, String newData) {
        deviceDao.updateDeviceData(sn, newData);
    }

    public void globalDataSync() {
        // 这里可以调用 Retrofit 同步接口，获取最新设备数据并更新到 Room 数据库
        // 例如：
        // retrofit.getApi().getAllDevices(userManager.getUserId()).enqueue(new Callback<ApiResponse<List<Device>>>() { ... });
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
