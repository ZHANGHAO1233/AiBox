package com.box.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.box.utils.ILog;
import com.idata.iot.sdk.api.IDataApi;
import com.notebook.BdManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.box.utils.ILog.TIME_TAG;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_INITED;
import static com.notebook.BdManager.TRANSACTION_STATUS_INITED;
import static com.notebook.BdManager.TRANSACTION_STATUS_RESULT;

/**
 * Created by Curry on 2018/7/16.
 */

public class OsModule {
    private static final String TAG = "OsModule";
    private static OsModule osOperator;
    private ServerModule server;
    private Handler handler;

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
//        return IDataApi.System.getDeviceSn();
        return "1688619";
    }

    public void lock() {//上锁
        ILog.d(TIME_TAG, new Date().getTime() + ",接收到关锁指令");
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (unlock) {
            ILog.d(TAG, "--lock  door");
            IDataApi.XblDoorLock.lock(lockResultListener);
        } else {
            sendLockStatus2Server(false);
        }
    }

    public void captureImageBeforeunlock() {
        if (this.handler != null) {
            Message message = new Message();
            message.what = HANDLER_MESSAGE_WHAT_INITED;
            handler.sendMessage(message);
        }
        //落锁
        ILog.d(TIME_TAG, new Date().getTime() + ",接收到开锁指令");
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        ILog.d(TAG, "当前门锁状态:" + (unlock ? "开启" : "关闭"));
        ILog.d(TAG, "当前订单状态:" + BdManager.transactionStatus);
        if (!unlock || BdManager.transactionStatus == TRANSACTION_STATUS_RESULT) {
            BdManager.transactionStatus = TRANSACTION_STATUS_INITED;
            //取照片
            ILog.d(TAG, "--captureImageBeforeunlock  door before,get the picture:");
            for (OnDoorStatusListener listener : doorListeners) {
                listener.onDoorOpen();
            }
//            ILog.d(TAG,"--captureImageBeforeunlock  door,listener:");
//            IDataApi.XblDoorLock.captureImageBeforeunlock(lockResultListener);
        } else {
            sendLockStatus2Server(true);
        }
    }

    public void unlock() {
        boolean unlock = IDataApi.XblDoorLock.isLockDown();//读取锁状态，返回true是落锁状态，返回false是上锁状态
        if (!unlock) {
            ILog.d(TAG, "--captureImageBeforeunlock  door,listener:");
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
            ILog.d(TAG, "onLockStatusChanged b:" + b);
        }

        @Override
        public void onDoorStatusChanged(final boolean b) {
            ILog.d(TAG, "onDoorStatusChanged b:" + b);
            if (b) {//门开了
                ILog.d(TIME_TAG, new Date().getTime() + ",冰箱门打开了");
                sendLockStatus2Server(b);
//                for (OnDoorStatusListener listener : doorListeners) {
//                    listener.onDoorOpen();
//                }
            } else {//门关了
                ILog.d(TIME_TAG, new Date().getTime() + ",冰箱门关闭了");
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
        Map<String, Object> map = new HashMap<>();
        map.put("sid", UUID.randomUUID().toString());
        map.put("cmd", "RetailStatus");
        map.put("status", open ? "Opened" : "Closed");
        String json = new JSONObject(map).toString();
        sendMsg(json);
    }

    public void sendRecognizeResult(JSONArray data) {//需要加入重试机制
        Map<String, Object> map = new HashMap<>();
        map.put("sid", UUID.randomUUID().toString());
        map.put("cmd", "CommodityStatus");
        map.put("data", data);
        String json = new JSONObject(map).toString();
        ILog.d(TAG, "send result 2 server,result:" + json);
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

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public interface OnDoorStatusListener {

        void onDoorOpen();

        void onDoorClose();

    }
}
