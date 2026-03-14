package com.agonylua.smarthome.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.agonylua.smarthome.database.entity.Device;
import com.agonylua.smarthome.repository.DeviceRepository;

import java.util.List;

public class MonitorViewModel extends AndroidViewModel {

    private final DeviceRepository repository;

    private static final String TAG = "MonitorViewModel";
    private LiveData<List<Device>> onlineDevices;
    private LiveData<Integer> onlineCount;

    public MonitorViewModel(@NonNull Application application) {
        super(application);
        repository = new DeviceRepository(application);
        onlineCount = repository.getOnlineCount();
        onlineDevices = repository.getOnlineDevices();
    }


    public LiveData<List<Device>> getOnlineDevices() {
        return onlineDevices;
    }

    public LiveData<Integer> getOnlineCount() {
        return onlineCount;
    }
}