package com.idata.aibox.business;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.idata.aibox.utils.MyLog;

/**
 * Created by Curry on 2018/7/23.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            MyLog.d("开机完成了!");
            MyLog.d("broadcast start service!");
            if (!WebsocketService.isServiceRunning("com.idata.aibox.business.WebsocketService", context)) {
                context.startService(new Intent(context, WebsocketService.class));
            }
        }
    }
}
