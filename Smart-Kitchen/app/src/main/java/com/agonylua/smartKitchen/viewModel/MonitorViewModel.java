package com.agonylua.smartKitchen.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.dto.DevicePowerDTO;
import com.agonylua.smartKitchen.repository.MonitorRepository;
import com.agonylua.smartKitchen.utils.UserManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MonitorViewModel extends ViewModel {

    private final MonitorRepository repository;

    private static final String TAG = "MonitorViewModel";
    private LiveData<List<Device>> onlineDevices;
    private LiveData<Integer> onlineCount;
    private MutableLiveData<List<DevicePowerDTO>> PowerData = new MutableLiveData<>();
    public MutableLiveData<Boolean> refreshResult = new MutableLiveData<>();
    private UserManager userManager;

    @Inject
    public MonitorViewModel(MonitorRepository repository, UserManager userManager) {
        onlineCount = repository.getOnlineCount();
        onlineDevices = repository.getOnlineDevices();
        this.repository = repository;
        this.userManager = userManager;
        refreshData();
    }

    public void refreshData() {
        getDevicePowerData();
        repository.getDevices(userManager.getHomeId(), new MonitorRepository.DeviceListCallback() {
            @Override
            public void onSuccess() {
                refreshResult.postValue(true);
            }

            @Override
            public void onFailure(String error) {
                Log.d(TAG, "refreshData onFailure: " + error);
                refreshResult.postValue(false);
            }
        });
    }

    public void getDevicePowerData() {
        String homeId = userManager.getHomeId();
        repository.getDevicePowerData(homeId, new MonitorRepository.poserCallback() {
            @Override
            public void onSuccess(List<DevicePowerDTO> data) {
                PowerData.postValue(data);
            }

            @Override
            public void onFailure(String error) {
                Log.d(TAG, "onFailure: " + error);
            }
        });
    }

    public LiveData<List<Device>> getOnlineDevices() {
        return onlineDevices;
    }

    public LiveData<Integer> getOnlineCount() {
        return onlineCount;
    }

    public MutableLiveData<List<DevicePowerDTO>> getPowerData() {
        return PowerData;
    }

    public void setPowerData(List<DevicePowerDTO> powerData) {
        PowerData.setValue(powerData);
    }
}