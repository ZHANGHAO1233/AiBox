package com.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Curry on 2018/10/18.
 */

public class ThreadPoolExecutorUtil {

    private final static int CORE_SIZE=5;
    private final static int MAX_POOL_SIZE =10;
    private final static  long KEEP_ALIVE_TIME = 30*60*1000;
    private static ScheduledThreadPoolExecutor executor;

    static{
        executor = new ScheduledThreadPoolExecutor(CORE_SIZE);
        executor.setMaximumPoolSize(MAX_POOL_SIZE);
        executor.setKeepAliveTime(KEEP_ALIVE_TIME, TimeUnit.MICROSECONDS);
    }

    public static void execute(Runnable task){
        executor.execute(task);
    }

    public static ScheduledFuture schedule(Runnable task , long millis){
        return executor.schedule(task, millis, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture schedule(Runnable task ,long millis,TimeUnit unit){
        return executor.schedule(task, millis,unit);
    }

    public static ScheduledFuture scheduleWithFixedDelay(Runnable task ,long initialDelay,long delay,TimeUnit unit){
        return executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public static boolean remove(Runnable task){
        return executor.remove(task);
    }

    public synchronized static void destory(){
        synchronized(ThreadPoolExecutorUtil.class){
            if (null!=executor&&!executor.isShutdown()) {
                try {
                    executor.shutdown();
                    try {
                        if(!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                            executor.shutdownNow();
                            if(!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                                executor.shutdownNow();
                            }
                        }
                    } catch (InterruptedException e) {
                        executor.shutdownNow();
                    }
                } finally {
                    executor = null;
                }
            }
        }
    }
}
