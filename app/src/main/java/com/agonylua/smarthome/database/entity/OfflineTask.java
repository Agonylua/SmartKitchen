package com.agonylua.smarthome.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_tasks")
public class OfflineTask {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "task_type")
    public String taskType; // 任务类型，如 "UPDATE_USER", "UPDATE_SCENE"

    @ColumnInfo(name = "payload")
    public String payload; // 任务携带的 JSON 数据参数

    @ColumnInfo(name = "created_at")
    public long createdAt; // 任务创建时间
}