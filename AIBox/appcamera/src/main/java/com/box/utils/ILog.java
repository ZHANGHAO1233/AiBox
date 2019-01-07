package com.box.utils;

/**
 * Created by leetn on 2016/11/14.
 */

import android.util.Log;

import com.box.utils.LogUtil;

/**
 * Created by leetn on 2016/10/27.
 */
public class ILog {
    public static final String BUSINESS_LOG = "box_business_log";

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        LogUtil.writeMsg(tag,msg);
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
