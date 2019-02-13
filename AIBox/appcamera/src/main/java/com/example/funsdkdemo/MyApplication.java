package com.example.funsdkdemo;

import android.app.Application;
import android.content.Context;

import com.box.core.OsModule;
import com.box.utils.CrashHandler;
import com.box.utils.ILog;
import com.box.utils.LogUtil;
import com.box.utils.NetworkUtil;
import com.example.download.XDownloadFileManager;
import com.idata.iot.sdk.api.IDataApi;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.FunSupport;
import com.mgr.ConfigPropertiesManager;
import com.mgr.ImageCacheManager;
import com.mgr.UncaughtExceptionManager;
import com.mgr.serial.comn.SerialPortsManager;
import com.notebook.BdManager;


public class MyApplication extends Application {
    private static Context context;
    private static MyApplication application;

    public static MyApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        LogUtil.init();
        ImageCacheManager.getInstance().startImageCacheCleaning();
        NetworkUtil.init(this);
        CrashHandler.getInstance().init(this);
        OsModule.get().init(this);
        ILog.d("定时重启时间：" + IDataApi.System.getTimeReboot());
        /**
         * 以下是FunSDK初始化
         */
        FunSupport.getInstance().init(this);

        /**
         * 以下是网络图片下载等的本地缓存初始化,可以加速图片显示,和节省用户流量
         * 跟FunSDK无关,只跟com.example.download内容相关
         */
        String cachePath = FunPath.getCapturePath();
        XDownloadFileManager.setFileManager(
                cachePath,                // 缓存目录
                20 * 1024 * 1024        // 20M的本地缓存空间
        );

        //新添加的代码
        IDataApi.init(this);
//        IDataApi.System.requestRouteToHost(ConnectivityManager.TYPE_ETHERNET, CameraConfigs.ips[0]);
        ConfigPropertiesManager.getInstance().init(this);
        UncaughtExceptionManager.getInstance().init();
    }

    public static Context getContext() {
        return context;
    }

    public void exit() {
        FunSupport.getInstance().term();
    }

}
