package com.agonylua.smarthome.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.agonylua.smarthome.database.entity.Device;

import java.util.List;

@Dao
public interface DeviceDao {
    // 查：返回 LiveData，这样数据库一变，UI 自动变
    @Query("SELECT * FROM devices WHERE homeId = :homeId")
    LiveData<List<Device>> getDevicesListByHomeId(String homeId);

    @Query("SELECT * FROM devices WHERE deviceSn = :deviceSn")
    LiveData<Device> getDeviceDataBySn(String deviceSn);

    @Query("SELECT * FROM devices WHERE homeId = :homeId")
    List<Device> getDevicesByHomeId(String homeId);

    @Query("SELECT COUNT(*) FROM devices")
    int getCount();

    @Query("SELECT COUNT(*) FROM devices WHERE deviceStatus = 'ONLINE'")
    LiveData<Integer> getOnlineCount();

    @Query("SELECT * FROM devices WHERE deviceMode != 'IDLE'")
    LiveData<List<Device>> getOnlineDevices();

    // 增/改：如果有重复的 SN 码，直接覆盖 (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Device> devices);

    // 改：单独更新某个设备 (用于编辑功能)
    @Update
    void update(Device device);

    @Query("UPDATE devices SET deviceData = :newData WHERE deviceSn = :sn")
    void updateDeviceData(String sn, String newData);

    @Query("UPDATE devices SET deviceMode = :newMode WHERE deviceSn = :sn")
    void updateDeviceMode(String sn, String newMode);

    @Query("UPDATE devices SET deviceStatus = :newStatus WHERE deviceSn = :sn")
    void updateDeviceStatus(String sn, String newStatus);

    // 删：清空缓存 (可选)
    @Query("DELETE FROM devices")
    void clearAll();
}

