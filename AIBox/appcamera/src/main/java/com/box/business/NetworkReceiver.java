package com.box.business;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.box.core.ServerModule;
import com.box.utils.ILog;
import com.box.utils.NetworkUtil;


/**
 * Created by Curry on 2018/7/23.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private Handler handler = new Handler();
    private Context context;
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            ILog.d("网络状态发生了变化!!!");
            NetworkUtil.isNetworkConnected = NetworkUtil.isNetworkAvailable(context);
            if (NetworkUtil.isNetworkConnected) {
                //检查重连
                if (!ServerModule.getServer().isConnecting()) {
                    ServerModule.getServer().reconnet();
                }
            }
        }
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.context = context;
        handler.postDelayed(run, 500);
    }
}
