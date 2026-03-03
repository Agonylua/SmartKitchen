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

    private LiveData<List<Device>> runningDevices;

    public MonitorViewModel(@NonNull Application application) {
        super(application);
        repository = new DeviceRepository(application);

    }


    public LiveData<List<Device>> getRunningDevices() {
        runningDevices = repository.getOnlineDevices();
        return runningDevices;
    }

}