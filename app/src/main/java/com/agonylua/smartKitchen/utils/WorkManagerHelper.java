package com.agonylua.smartKitchen.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.agonylua.smartKitchen.service.GlobalSyncWorker;

import java.util.concurrent.TimeUnit;

/**
 * 集中管理 WorkManager 任务的调度
 */
public class WorkManagerHelper {

    public static final String SYNC_WORK_NAME = "PERIODIC_DEVICE_SYNC_WORK";
    public static final String TEST_SYNC_WORK_NAME = "TEST_ONE_TIME_SYNC_WORK";

    /**
     * 测试专用：启动一次性同步任务，方便立即观察 Log 且不等待 15 分钟
     */
    public static void testOneTimeDataSync(Context context) {
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(
                GlobalSyncWorker.class
        ).build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                TEST_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE, // 每次调用都替换现有的立即执行
                syncRequest
        );
    }

    /**
     * 启动定期数据同步任务
     * 建议在用户登录成功后，或者 Application 的 onCreate 中调用
     */
    public static void startPeriodicDataSync(Context context) {
        // 设置任务运行约束条件
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 必须在有网络连接时才执行
                .setRequiresBatteryNotLow(true)                // 手机电量不能太低
                .build();

        // 创建周期性任务请求
        // 注意：Android WorkManager 规定的最小周期是 15 分钟
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                GlobalSyncWorker.class,
                15, TimeUnit.MINUTES // 每 15 分钟执行一次
        )
                .setConstraints(constraints)
                .build();

        // 将任务加入调度队列
        // 使用 enqueueUniquePeriodicWork 防止同一任务被重复调度
        // ExistingPeriodicWorkPolicy.KEEP 表示如果任务已存在，则保持原有任务运行，不替换
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    /**
     * 取消定期同步任务
     * 建议在用户退出登录 (Logout) 时调用
     */
    public static void stopPeriodicDataSync(Context context) {
        WorkManager.getInstance(context.getApplicationContext())
                .cancelUniqueWork(SYNC_WORK_NAME);
    }
}