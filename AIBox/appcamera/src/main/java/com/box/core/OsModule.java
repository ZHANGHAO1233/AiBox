package com.box.core;

import android.content.Context;

import com.box.utils.ILog;
import com.idata.iot.sdk.api.IDataApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Curry on 2018/7/16.
 */

public class OsModule {
    private static OsModule osOperator;
    private ServerModule server;

    private OsModule() {
        server = ServerModule.getServer();
    }

    public static OsModule get() {
        if (osOperator == null) {
            synchronized (OsModule.class) {
                if (osOperator == null) {
                    osOperator = new OsModule();
                }
            }
        }
        return osOperator;
    }

    public void init(Context context) {
        IDataApi.init(context);
        //保活机制
        IDataApi.Watchdog.addWatchdog("com.idata.aibox", "com.MainActivity");
        IDataApi.XblDoorLock.setLockStatusListener(lockStatusListener);//所状态监听
    }

    public String getSn() {
        return IDataApi.System.getDeviceSn();
    }

    public void lock() {//上锁
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (unlock) {
            ILog.d("--lock  door");
            IDataApi.XblDoorLock.lock(lockResultListener);
        } else {
            sendLockStatus2Server(false);
        }
    }

    public void captureImageBeforeunlock() {//落锁
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (!unlock) {
            //取照片
            ILog.d("--captureImageBeforeunlock  door before,get the picture:");
            for (OnDoorStatusListener listener : doorListeners) {
                listener.onDoorOpen();
            }
//            ILog.d("--captureImageBeforeunlock  door,listener:");
//            IDataApi.XblDoorLock.captureImageBeforeunlock(lockResultListener);
        } else {
            sendLockStatus2Server(true);
        }
    }

    public void unlock(){
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (!unlock) {
            ILog.d("--captureImageBeforeunlock  door,listener:");
            IDataApi.XblDoorLock.unlock(lockResultListener);
        }
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
            ILog.d("onLockStatusChanged b:" + b);
        }

        @Override
        public void onDoorStatusChanged(final boolean b) {
            ILog.d("onDoorStatusChanged b:" + b);
            if (b) {//门开了
                sendLockStatus2Server(b);
//                for (OnDoorStatusListener listener : doorListeners) {
//                    listener.onDoorOpen();
//                }
            } else {//门关了
                sendLockStatus2Server(b);
                for (OnDoorStatusListener listener : doorListeners) {
                    listener.onDoorClose();
                }
                if (!IDataApi.XblDoorLock.isDoorOpen()) {
                    lock();
                }
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

    public void sendRecognizeResult(JSONArray data) {//需要加入重试机制
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", UUID.randomUUID().toString());
        map.put("cmd", "CommodityStatus");
        map.put("data", data);
        String json = new JSONObject(map).toString();
        ILog.d("send result 2 server,result:" + json);
        sendMsg(json);
    }

    private List<OnDoorStatusListener> doorListeners = new ArrayList<>();

    public void addDoorListener(OnDoorStatusListener listener) {
        if (!doorListeners.contains(listener)) {
            doorListeners.add(listener);
        }
    }

    public void removeDoorListener(OnDoorStatusListener listener) {
        if (doorListeners.contains(listener)) {
            doorListeners.remove(listener);
        }
    }

    public interface OnDoorStatusListener {

        void onDoorOpen();

        void onDoorClose();

    }
}
