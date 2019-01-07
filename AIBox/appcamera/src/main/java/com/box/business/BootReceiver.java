package com.box.business;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.MainActivity;
import com.box.utils.ILog;


/**
 * Created by Curry on 2018/7/23.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            ILog.d("reboot finish,broadcast start app!");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
//            if (!WebsocketService.isServiceRunning("com.idata.aibox.business.WebsocketService", context)) {
//                context.startService(new Intent(context, WebsocketService.class));
//            }
        }
    }
}
