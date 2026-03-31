package com.agonylua.smarthome.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.dao.OfflineTaskDao;
import com.agonylua.smarthome.database.entity.OfflineTask;

import java.io.IOException;
import java.util.List;

/**
 * 离线任务同步 Worker
 * 当系统检测到网络恢复时，会自动触发此 Worker 执行断网期间积压的弱实时性任务
 */
public class OfflineTaskWork extends Worker {

    private static final String TAG = "OfflineTaskWork";
    private final OfflineTaskDao taskDao;

    public OfflineTaskWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // 初始化依赖
        taskDao = AppDatabase.getInstance(context).offlineTaskDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "开始执行离线任务同步");

        // 1. 获取所有待处理的任务
        List<OfflineTask> pendingTasks = taskDao.getAllPendingTasks();
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return Result.success();
        }

        boolean hasFailure = false;

        // 2. 遍历执行任务
        for (OfflineTask task : pendingTasks) {
            boolean success = false;
            try {
                success = executeSingleTask(task);
            } catch (Exception e) {
                Log.e(TAG, "任务执行异常: " + task.taskType, e);
            }

            if (success) {
                // 3. 执行成功，从本地数据库中删除该任务
                taskDao.delete(task);
                Log.d(TAG, "离线任务同步成功: " + task.taskType);
            } else {
                // 如果有任务失败，标记为失败，稍后 WorkManager 会根据退避策略重试
                hasFailure = true;
                Log.w(TAG, "离线任务同步失败，稍后重试: " + task.taskType);
            }
        }

        // 如果有失败的任务，返回 retry 通知系统稍后再次执行
        return hasFailure ? Result.retry() : Result.success();
    }

    /**
     * 根据任务类型路由并执行具体的 HTTP 请求
     * 注意：这里使用的是同步请求 (execute)，因为 doWork 本身就在后台线程运行
     */
    private boolean executeSingleTask(OfflineTask task) throws IOException {
        switch (task.taskType) {
            case "UPDATE_USER_PROFILE":
                // 例子：同步修改的用户信息
//                UserRequest userReq = JsonUtils.fromJson(task.payload, UserRequest.class);
//                Response<ApiResponse<UserDTO>> response = apiService.updateUserProfile(userReq).execute();
//                return response.isSuccessful() && response.body() != null && response.body().getCode() == 200;

                return true;

            case "UPDATE_DEVICE_NAME":
                // 例子：同步修改的设备名称
                // DeviceConfigReq req = JsonUtils.fromJson(task.payload, DeviceConfigReq.class);
                // Response<ApiResponse<Void>> devResponse = apiService.updateDeviceConfig(req).execute();
                // return devResponse.isSuccessful();
                return true;

            // ... 添加其他弱实时性任务分支 ...

            default:
                Log.e(TAG, "未知的离线任务类型: " + task.taskType);
                return true; // 未知任务直接丢弃，避免死循环重试
        }
    }
}