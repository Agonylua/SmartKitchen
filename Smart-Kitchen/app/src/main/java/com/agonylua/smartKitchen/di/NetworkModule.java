package com.agonylua.smartKitchen.di;

import android.app.Application;
import android.content.Context;

import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.EspProvisioningHelper;
import com.agonylua.smartKitchen.utils.NetworkMonitor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public RetrofitClient provideRetrofitClient(@ApplicationContext Context context) {
        return RetrofitClient.getInstance(context);
    }

    @Provides
    @Singleton
    public NetworkMonitor provideNetworkMonitor(@ApplicationContext Context context) {
        return NetworkMonitor.getInstance((Application) context);
    }

    @Provides
    @Singleton
    public EspProvisioningHelper provideEspProvisioningHelper(@ApplicationContext Context context) {
        return EspProvisioningHelper.getInstance(context);
    }

}

