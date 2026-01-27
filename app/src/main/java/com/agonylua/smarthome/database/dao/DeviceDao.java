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
    LiveData<List<Device>> getDevicesByHome(String homeId);

    @Query("SELECT * FROM devices WHERE deviceName = :deviceName")
    Device getDeviceDataByName(String deviceName);

    @Query("SELECT COUNT(*) FROM devices")
    int getCount();

    // 增/改：如果有重复的 SN 码，直接覆盖 (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Device> devices);

    // 改：单独更新某个设备 (用于编辑功能)
    @Update
    void update(Device device);

    // 删：清空缓存 (可选)
    @Query("DELETE FROM devices")
    void clearAll();
}
