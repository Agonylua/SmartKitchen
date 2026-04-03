package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.dto.DevicePowerDTO;
import com.agonylua.smarthome.repository.MonitorRepository;
import com.agonylua.smarthome.utils.UserManager;

import java.util.List;

public class MonitorViewModel extends AndroidViewModel {

    private final MonitorRepository repository;

    private static final String TAG = "MonitorViewModel";
    private LiveData<List<Device>> onlineDevices;
    private LiveData<Integer> onlineCount;
    private final MutableLiveData<List<DevicePowerDTO>> PowerData = new MutableLiveData<>();
    public MutableLiveData<Boolean> refreshResult = new MutableLiveData<>();
    private UserManager userManager;

    public MonitorViewModel(@NonNull Application application) {
        super(application);
        repository = MonitorRepository.getInstance(application);
        onlineCount = repository.getOnlineCount();
        onlineDevices = repository.getOnlineDevices();
        userManager = UserManager.getInstance(application);
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
        String homeId = UserManager.getInstance(getApplication()).getHomeId();
        repository.getDevicePowerData(homeId, getApplication(), new MonitorRepository.poserCallback() {
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

}