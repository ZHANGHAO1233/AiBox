package com.idata.aibox.business;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.idata.aibox.core.ServerModule;
import com.idata.aibox.utils.MyLog;

import java.util.List;


public class WebsocketService extends Service {

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.d("start service:onStartCommand()");
        ServerModule.getServer().connetToServer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServerModule.getServer().disconnect();
    }

    /**
     * 判断服务是否运行
     */
    public static boolean isServiceRunning(final String className, Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

}
