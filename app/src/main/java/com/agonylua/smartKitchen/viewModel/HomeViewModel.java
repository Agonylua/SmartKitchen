package com.agonylua.smartKitchen.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.repository.HomeRepository;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;
import com.agonylua.smartKitchen.utils.UserManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private HomeRepository repository;
    private DeviceDao deviceDao;
    private UserManager userManager;

    private final String TAG = "HomeViewModel";
    private MutableLiveData<String> userName = new MutableLiveData<>();
    private LiveData<List<Device>> deviceList = new MutableLiveData<>();
    private LiveData<Integer> deviceCount = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRefresh = new MutableLiveData<>(false);
    private MutableLiveData<Float> tempSensor = new MutableLiveData<>(0f);

    @Inject
    public HomeViewModel(HomeRepository repository, DeviceDao deviceDao, UserManager userManager) {
        this.repository = repository;
        this.deviceDao = deviceDao;
        this.userManager = userManager;
        syncServiceData(userManager.getHomeId());
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
        repository.getDevices(homeId, new HomeRepository.DeviceListCallback() {
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
        repository.deleteDevice(null, deviceSn, new HomeRepository.DeleteDeviceCallback() {
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
        userName.setValue(userManager.getUserName());
        return userName;
    }

    public MutableLiveData<Boolean> getIsRefresh() {
        return isRefresh;
    }
}
