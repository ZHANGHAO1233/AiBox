package com.idata.aibox.core;

import android.content.Context;
import android.os.Handler;

import com.idata.aibox.MainActivity;
import com.idata.aibox.utils.MyLog;
import com.idata.iot.sdk.api.IDataApi;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Curry on 2018/7/16.
 */

public class OsModule {

    private static OsModule osOperator;
    private ServerModule server;
    private Handler handler=new Handler();

    private OsModule() {
        server = ServerModule.getServer();
    }

    public static OsModule getOsOperator() {
        if (osOperator == null) {
            osOperator = new OsModule();
        }
        return osOperator;
    }

    public void init(Context context) {
        IDataApi.init(context);
        //保活机制
        IDataApi.Watchdog.addWatchdog("com.idata.aibox", "com.idata.aibox.MainActivity");
        IDataApi.XblDoorLock.setLockStatusListener(lockStatusListener);//所状态监听
    }

    public String getSn() {

        return IDataApi.System.getDeviceSn();
    }


    public void lock() {//上锁
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (unlock) {
            IDataApi.XblDoorLock.lock(lockResultListener);
        } else {
            sendLockStatus2Server(false);
        }
    }

    public void unlock() {//落锁
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (!unlock) {
            IDataApi.XblDoorLock.unlock(lockResultListener);
//            testUnlock();
        } else {
            sendLockStatus2Server(true);
        }
    }

    private void testUnlock() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lock();
            }
        }, 20000);
    }

    public IDataApi.LockResultListener lockResultListener = new IDataApi.LockResultListener() {
        @Override
        public void onSuccess() {
//            sendLockStatus2Server(true);
        }

        @Override
        public void onFail() {
//            sendLockStatus2Server(false);
        }
    };

    public IDataApi.LockStatusListener lockStatusListener = new IDataApi.LockStatusListener() {
        @Override
        public void onLockStatusChanged(boolean b) {
            sendLockStatus2Server(b);
        }

        @Override
        public void onDoorStatusChanged(final boolean b) {
            if (!IDataApi.XblDoorLock.isDoorOpen()){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!IDataApi.XblDoorLock.isDoorOpen()){
                            lock();
                        }
                    }
                },1000);
            }
        }
    };

    private void sendMsg(String msg) {
        server.sendMessage(msg);
    }

    public void sendLockStatus2Server(boolean open) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", UUID.randomUUID().toString());
        map.put("cmd", "RetailStatus");
        map.put("status", open ? "Opened" : "Closed");
        String json = new JSONObject(map).toString();
        sendMsg(json);
    }
}
