package com.agonylua.smarthome.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.Model.Device;
import com.agonylua.smarthome.Repository.HomeRepository;
import com.agonylua.smarthome.Utils.TokenManager;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;
    private TokenManager tokenManager;
    private String TAG = "HomeViewModel";

    private MutableLiveData<List<Device>> deviceList = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository();
    }

    /**
     * 加载设备列表
     *
     * @param homeId 家庭ID
     */
    public void loadDevices(String homeId) {
        //isLoading.setValue(true); // 显示加载条
        Log.d(TAG, "loadDevices: 请求设备列表，homeId=" + homeId);
        repository.getDevices(getApplication(), homeId, new HomeRepository.DeviceCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                //isLoading.setValue(false); // 隐藏加载条
                deviceList.setValue(devices); // 更新列表数据
            }

            @Override
            public void onError(String message) {
                //isLoading.setValue(false); // 隐藏加载条
                errorMessage.setValue(message); // 发送错误信息
            }
        });
    }

    /**
     * 验证Token有效性
     *
     * @return Token是否有效
     */
    public Boolean validateToken() {
        tokenManager = new TokenManager(getApplication());
        if (tokenManager.getToken() == null) {
            Log.d(TAG, "validateToken: Token is null");
            return false;
        }
        String token = tokenManager.getToken();
        final Boolean[] isValid = {false};
        repository.validateToken(getApplication(), token, new HomeRepository.DeviceCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                Log.d(TAG, "validateToken: Token 有效");
                isValid[0] = true;
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message); // 发送错误信息
                Log.d(TAG, "validateToken: Token 无效，错误信息: " + message);
                isValid[0] = false;
            }
        });
        return isValid[0];
    }

    // Getters for LiveData
    public LiveData<List<Device>> getDeviceList() {
        return deviceList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
