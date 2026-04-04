package com.agonylua.smartKitchen.di;

import android.content.Context;

import com.agonylua.smartKitchen.database.AppDatabase;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.dao.HomeDao;
import com.agonylua.smartKitchen.database.dao.RulesDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public DeviceDao provideDeviceDao(AppDatabase database) {
        return database.deviceDao();
    }

    @Provides
    public HomeDao provideHomeDao(AppDatabase database) {
        return database.homeDao();
    }

    @Provides
    public RulesDao provideRulesDao(AppDatabase database) {
        return database.rulesDao();
    }
}
