package com.idata.aibox.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by DELL on 2017/12/4.
 */

public class NetworkUtil {
    public static boolean isNetworkConnected = true;


    //初始化网络状态信息
    public static void init(Context c) {
        isNetworkConnected = isNetworkAvailable(c);
    }

    //检测网络是否可用
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取WIFI连接的信息
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Boolean isWifiConn = networkInfo.isConnected();
        //获取移动数据连接的信息
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Boolean isMobileConn = networkInfo.isConnected();
        MyLog.d("当前连接的网络信息：" + ":iswifi:" + isWifiConn + ":ismobile:" + isMobileConn);
        return isWifiConn || isMobileConn;
    }
}
