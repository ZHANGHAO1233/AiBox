package com.mgr;

import com.box.utils.ILog;

/**
 * 程序异常处理类
 *
 * @author zhangh
 * @version 1.0.1
 */
public class UncaughtExceptionManager implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "UncaughtExceptionManager";
    private static UncaughtExceptionManager sManager;

    private UncaughtExceptionManager() {
    }

    public static UncaughtExceptionManager getInstance() {
        if (sManager == null) {
            sManager = new UncaughtExceptionManager();
        }
        return sManager;
    }

    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ILog.d(TAG, "应用程序于线程" + Thread.currentThread().getName() + "崩溃，崩溃消息:\n " + e.getMessage());
    }
}
