package com.idata.aibox;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.idata.aibox.core.OsModule;
import com.idata.aibox.utils.CrashHandler;
import com.idata.aibox.utils.LogUtil;
import com.idata.aibox.utils.MyLog;
import com.idata.aibox.utils.NetworkUtil;
import com.idata.iot.sdk.api.IDataApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Curry on 2018/7/16.
 */

//https://juejin.im/post/5aaf165b518825556f5537f7

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        LogUtil.init(this);
        NetworkUtil.init(this);
        CrashHandler.getInstance().init(this);
        OsModule.getOsOperator().init(this);
        MyLog.d("定时重启时间："+IDataApi.System.getTimeReboot());
    }

    public static Context getContext(){
        return context;
    }
}
