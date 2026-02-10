package com.agonylua.smarthome.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.agonylua.smarthome.database.dao.DeviceDao;
import com.agonylua.smarthome.database.entity.Device;

@Database(entities = {Device.class}, version = 2, exportSchema = false)
//@TypeConverters(DataConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase mAppDatabase;

    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "smartKitchen_db")
                            // 添加数据库迁移策略
                            .addMigrations()
                            // 默认不允许在主线程中连接数据库
                            // TODO : 生产环境中应避免使用此选项
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return mAppDatabase;
    }

    public abstract DeviceDao deviceDao();
}
