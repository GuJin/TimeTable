package com.sunrain.timetablev4.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

import tech.gujin.toast.ToastUtil;

public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        ToastUtil.initialize(sContext, ToastUtil.Mode.REPLACEABLE);
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }
}
