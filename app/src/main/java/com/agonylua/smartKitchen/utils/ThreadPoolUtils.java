package com.agonylua.smartKitchen.utils;

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
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT + 1, 4));

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

    /**
     * 执行带超时监测的任务
     * <p>
     * 原理：提交任务后获得 Future 控制句柄，同时利用 Handler 开启一个倒计时。
     * 如果倒计时结束任务仍未完成，则尝试中断任务线程并执行回调。
     *
     * @param task            需要在子线程执行的耗时任务
     * @param timeoutMillis   超时时间 (毫秒)
     * @param timeoutCallback 超时发生时的回调 (运行在主线程，适合更新 UI)
     */
    public void executeWithTimeout(Runnable task, long timeoutMillis, Runnable timeoutCallback) {
        if (task == null) return;

        // 提交任务到线程池，并获取 Future 对象以便后续控制
        // 注意：这里直接调用 threadPoolExecutor.submit 以获取 Future，而不是用 execute()
        Future<?> future = threadPoolExecutor.submit(task);

        // 在主线程延迟执行超时检查
        mainHandler.postDelayed(() -> {
            // 检查任务状态：如果任务尚未完成 (isDone() 为 false)
            if (!future.isDone()) {
                // 尝试取消任务
                // true 表示如果线程正在运行，则允许中断 (抛出 InterruptedException)
                boolean isCancelled = future.cancel(true);

                // 如果取消成功（或任务确实超时未完），触发超时回调
                if (timeoutCallback != null) {
                    timeoutCallback.run();
                }
            }
        }, timeoutMillis);
    }

    // TODO 获取当前线程池状态信息 (调试用)
    public String getPoolStatus() {
        return String.format("Core: %d, Active: %d, Queue: %d, Completed: %d",
                threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount());
    }
}