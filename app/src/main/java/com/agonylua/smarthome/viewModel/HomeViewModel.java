package com.agonylua.smarthome.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.repository.HomeRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.TokenManager;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;
    private TokenManager tokenManager;
    private DeviceDao deviceDao;
    private String TAG = "HomeViewModel";

    private LiveData<List<Device>> deviceList = new MutableLiveData<>();
    private MutableLiveData<String> deviceCount = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository(getApplication());
        deviceDao = AppDatabase.getInstance(getApplication()).deviceDao();
    }

    /**
     * 加载设备列表
     *
     * @param homeId 家庭ID
     */
    public void loadDevices(String homeId) {
        try {
            repository.getDevices(getApplication(), homeId);
            ThreadPoolUtils.getInstance().execute(() -> {
                String count = String.valueOf(deviceDao.getCount());
                deviceCount.postValue(count);
            });
        } catch (Exception e) {
            errorMessage.setValue("加载设备失败: " + e.getMessage());
        }
    }

    // Getters for LiveData
    public LiveData<List<Device>> getDeviceList(String homeId) {
        deviceList = repository.getDeviceList(homeId);
        return deviceList;
    }

    public LiveData<String> getDeviceCount() {
        return deviceCount;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
