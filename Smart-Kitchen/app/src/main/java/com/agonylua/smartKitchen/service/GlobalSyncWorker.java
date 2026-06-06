package com.agonylua.smartKitchen.service;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.agonylua.smartKitchen.repository.GlobalRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * 全局数据同步 Worker
 * 由 Hilt 接管生命周期和依赖注入
 */
@HiltWorker
public class GlobalSyncWorker extends Worker {
    private static final String TAG = "GlobalSyncWorker";
    private final GlobalRepository globalRepository;

    @AssistedInject
    public GlobalSyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            GlobalRepository globalRepository) {
        super(context, workerParams);
        this.globalRepository = globalRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {

            globalRepository.syncAllData();

            Log.d(TAG, "doWork: 全局数据同步成功");
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
