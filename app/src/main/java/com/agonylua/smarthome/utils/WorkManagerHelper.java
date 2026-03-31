package com.agonylua.smarthome.utils;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.entity.OfflineTask;
import com.agonylua.smarthome.service.OfflineTaskWork;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {

    /**
     * 将任务加入离线队列并触发调度
     */
    public static void enqueueOfflineTask(Context context, String taskType, Object payloadObj) {
        ThreadPoolUtils.getInstance().execute(() -> {
            // 1. 序列化参数并保存到 Room 数据库
            OfflineTask task = new OfflineTask();
            task.taskType = taskType;
            task.payload = JsonUtils.toJson(payloadObj);
            task.createdAt = System.currentTimeMillis();

            AppDatabase.getInstance(context).offlineTaskDao().insert(task);

            // 2. 配置 WorkManager 约束条件：必须有网络连接才执行
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // 3. 构建工作请求 (添加指数退避策略以防服务器崩溃狂刷接口)
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(OfflineTaskWork.class)
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            WorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)
                    .build();

            // 4. 提交给 WorkManager
            // 使用 KEEP 策略：如果当前已经有一个同样的同步任务在排队或执行，则保持原样，不重复创建
            WorkManager.getInstance(context).enqueueUniqueWork(
                    "OFFLINE_TASK_SYNC",
                    ExistingWorkPolicy.KEEP,
                    syncWorkRequest
            );
        });
    }
}