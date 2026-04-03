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
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.UserManager;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;
    private DeviceDao deviceDao;
    private final String TAG = "HomeViewModel";
    private MutableLiveData<String> userName = new MutableLiveData<>();
    private LiveData<List<Device>> deviceList = new MutableLiveData<>();
    private LiveData<Integer> deviceCount = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRefresh = new MutableLiveData<>(false);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = HomeRepository.getInstance(getApplication());
        deviceDao = AppDatabase.getInstance(getApplication()).deviceDao();
    }

    /**
     * 加载设备列表
     *
     */
    public void loadDevices() {
        ThreadPoolUtils.getInstance().execute(() -> {
            deviceCount = deviceDao.getOnlineCount();
        });
    }

    public void syncServiceData(String homeId) {
        repository.getDevices(getApplication(), homeId, new HomeRepository.DeviceListCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                loadDevices();
                isRefresh.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                isRefresh.setValue(false);
                HomeViewModel.this.errorMessage.setValue(errorMessage);
            }
        });
    }

    public void deleteDevice(String deviceSn, String homeId) {
        repository.deleteDevice(getApplication(), deviceSn, new HomeRepository.DeleteDeviceCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "设备删除成功，准备拉取最新设备列表");
                syncServiceData(homeId);
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "设备删除失败，回滚拉取最新设备列表: " + errorMsg);
                errorMessage.setValue(errorMsg);
                syncServiceData(homeId);
            }
        });
    }

    // Getters for LiveData
    public LiveData<List<Device>> getDeviceList(String homeId) {
        deviceList = repository.getDeviceList(homeId);
        Log.d(TAG, "getDeviceList: " + deviceList);
        return deviceList;
    }

    public LiveData<Integer> getDeviceCount() {
        return deviceCount;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getUserName() {
        userName.setValue(UserManager.getInstance(getApplication()).getUserName());
        return userName;
    }

    public MutableLiveData<Boolean> getIsRefresh() {
        return isRefresh;
    }
}
