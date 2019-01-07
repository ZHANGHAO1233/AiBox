package com.idata.aibox.utils;

/**
 * Created by leetn on 2016/11/14.
 */

import android.util.Log;


/**
 * Created by leetn on 2016/10/27.
 */
//对系统Log进行简单封装，方便关闭Log
public class MyLog {
    private static final String BUSINESS_LOG = "aibox_test";

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void d(String msg) {//d:business :业务
        d(BUSINESS_LOG, msg);
        LogUtil.writeMsg(msg);
//        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String time = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格式的时间
//        LogUtil.writeMsg(time + ":" + msg + "\r\n" + "\r\n");
    }

    public static void e(String msg, Throwable tr) {
        e(BUSINESS_LOG, msg, tr);
    }

    public static void i(String msg) {
        i(BUSINESS_LOG, msg);
    }

    public static void v(String msg) {
        v(BUSINESS_LOG, msg);
    }

    public static void w(String msg) {
        w(BUSINESS_LOG, msg);
    }

}
