package com.agonylua.smartKitchen.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.agonylua.smartKitchen.database.entity.OfflineTask;

import java.util.List;

@Dao
public interface OfflineTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineTask task);

    // 按时间顺序获取所有未处理的任务
    @Query("SELECT * FROM offline_tasks ORDER BY created_at ASC")
    List<OfflineTask> getAllPendingTasks();

    @Delete
    void delete(OfflineTask task);
}