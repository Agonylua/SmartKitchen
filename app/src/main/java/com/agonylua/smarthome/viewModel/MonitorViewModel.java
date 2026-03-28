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

    public MonitorViewModel(@NonNull Application application) {
        super(application);
        repository = new MonitorRepository(application);
        onlineCount = repository.getOnlineCount();
        onlineDevices = repository.getOnlineDevices();
    }

    public void getDevicePowerData() {
        String homeId = UserManager.getInstance(getApplication()).getHomeId();
        repository.getDevicePowerData(homeId, getApplication(), new MonitorRepository.callback() {
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