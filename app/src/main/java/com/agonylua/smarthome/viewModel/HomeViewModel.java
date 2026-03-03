package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.repository.HomeRepository;
import com.agonylua.smarthome.utils.NetworkMonitor;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;
    private DeviceDao deviceDao;
    private final String TAG = "HomeViewModel";

    private LiveData<List<Device>> deviceList = new MutableLiveData<>();
    private MutableLiveData<String> deviceCount = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository(getApplication());
        deviceDao = AppDatabase.getInstance(getApplication()).deviceDao();
    }

    /**
     * 加载设备列表
     *
     */
    public void loadDevices() {
        ThreadPoolUtils.getInstance().execute(() -> {
            String count = String.valueOf(deviceDao.getCount());
            deviceCount.postValue(count);
        });
    }

    public void syncServiceData(String homeId) {
        if (NetworkMonitor.getInstance(getApplication()).isInternetReachable()) {
            errorMessage.setValue("无网络，请检查网络连接");
            return;
        }
        repository.getDevices(getApplication(), homeId, new HomeRepository.DeviceListCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                loadDevices();
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("网络异常" + error);
            }
        });
    }

    // Getters for LiveData
    public LiveData<List<Device>> getDeviceList(String homeId) {
        deviceList = repository.getDeviceList(homeId);
        Log.d(TAG, "getDeviceList: " + deviceList);
        return deviceList;
    }

    public LiveData<String> getDeviceCount() {
        return deviceCount;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
