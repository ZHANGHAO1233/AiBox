package com.example.funsdkdemo;

import android.app.Application;
import android.content.Context;

import com.lib.funsdk.support.FunSupport;


public class MyApplication extends Application {
    private static Context context;
    private static MyApplication application;

    public static MyApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public void exit() {
        FunSupport.getInstance().term();
    }

}
