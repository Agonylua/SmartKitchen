package com.agonylua.smarthome.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 智能家居 App 专用线程池工具类
 * 特点：自动计算 CPU 核心数、支持 UI 线程切换、拒绝策略安全
 */
public class ThreadPoolUtils {

    private static final String TAG = "ThreadPoolUtils";

    // 获取设备 CPU 核心数 (通常是 8 核)
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // 核心线程数：核心数 + 1 (适合计算密集型)
    // 如果是 IO 密集型 (网络请求多)，建议设置为 2 * CPU_COUNT + 1
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    // 最大线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    // 空闲线程存活时间 (秒)
    private static final int KEEP_ALIVE_SECONDS = 30;

    // 任务队列容量 (防止任务堆积过多导致 OOM)
    private static final int QUEUE_CAPACITY = 128;

    // 单例模式
    private static volatile ThreadPoolUtils instance;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final Handler mainHandler;

    /**
     * 私有构造函数，初始化线程池和主线程 Handler
     *
     * @see ThreadPoolExecutor
     */
    private ThreadPoolUtils() {
        // 自定义线程工厂 (为了给线程起名字，方便调试)
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "SmartKitchen-Thread");
            }
        };
        // 初始化线程池
        threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, // 核心线程数
                MAXIMUM_POOL_SIZE, // 最大线程数
                KEEP_ALIVE_SECONDS, // 空闲线程存活时间
                TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(QUEUE_CAPACITY), // 有界队列
                threadFactory, // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：如果队列满了，由调用线程自己去执行，不抛异常
        );

        // 初始化主线程 Handler
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // 获取单例
    public static ThreadPoolUtils getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolUtils.class) {
                if (instance == null) {
                    instance = new ThreadPoolUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 执行普通任务 (无返回值)
     * 适合：网络请求、写数据库
     */
    public void execute(Runnable runnable) {
        if (runnable != null) {
            threadPoolExecutor.execute(runnable);
        }
    }

    /**
     * 执行任务 (有返回值)
     * 适合：需要获取结果的计算
     */
    public <T> Future<T> submit(Callable<T> callable) {
        return threadPoolExecutor.submit(callable);
    }

    /**
     * 切换到主线程执行 (更新 UI)
     * 示例：ThreadPoolUtils.getInstance().runOnUiThread(() -> textView.setText("完成"));
     */
    public void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run(); // 如果当前已经是主线程，直接运行
        } else {
            mainHandler.post(runnable); // 否则 Post 到主线程
        }
    }

    /**
     * 延迟执行任务
     *
     * @param runnable    任务
     * @param delayMillis 延迟毫秒数
     */
    public void executeDelay(Runnable runnable, long delayMillis) {
        mainHandler.postDelayed(() -> execute(runnable), delayMillis);
    }

    /**
     * 移除任务 (防止 Activity 销毁后还在执行)
     */
    public void removeTask(Runnable runnable) {
        threadPoolExecutor.remove(runnable);
        mainHandler.removeCallbacks(runnable);
    }

    // 获取当前线程池状态信息 (调试用)
    public String getPoolStatus() {
        return String.format("Core: %d, Active: %d, Queue: %d, Completed: %d",
                threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount());
    }
}