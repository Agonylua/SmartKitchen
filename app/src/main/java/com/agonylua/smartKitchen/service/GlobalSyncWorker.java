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

    // 注意：必须使用 @AssistedInject，且 Context 和 WorkerParameters 必须带 @Assisted 注解
    @AssistedInject
    public GlobalSyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            GlobalRepository globalRepository) {
        super(context, workerParams);
        // Hilt 自动将单例的 Repository 注入到 Worker 中
        this.globalRepository = globalRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 注意：Worker 的 doWork() 天然在子线程运行。

            globalRepository.syncAllData();

            Log.d(TAG, "doWork: 全局数据同步成功");
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            // 如果拉取失败（如网络抖动），返回 retry()，WorkManager 会执行指数退避重试算法
            return Result.retry();
        }
    }
}
