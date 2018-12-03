package com.sunrain.timetablev4.manager;


import android.app.ActivityManager;
import android.content.Context;
import android.os.TransactionTooLargeException;

import com.sunrain.timetablev4.application.MyApplication;

import java.io.File;
import java.util.List;

import tech.gujin.toast.ToastUtil;


public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    public static CrashHandler getInstance() {
        return CrashHandlerHolder.INSTANCE;
    }

    public void init() {
        /*
         * 弹出解决方案之后把崩溃继续交给系统处理，
         * 所以保存当前UncaughtExceptionHandler用于崩溃发生时使用。
         */
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if (ex != null) {
            Throwable cause = ex.getCause();
            if (cause instanceof TransactionTooLargeException) {
                // 目前尚未知道TransactionTooLargeException崩溃原因，怀疑是壁纸过大
                File picFile = new File(MyApplication.sContext.getFilesDir(), WallpaperManager.FILE_NAME);
                if (picFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    picFile.delete();
                }
                ToastUtil.show("崩溃了，请邮件向itimetable@foxmail.com反馈", true, ToastUtil.Mode.NORMAL);
            }
        }

        ActivityManager am = (ActivityManager) MyApplication.sContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> list = null;
            try {
                list = am.getAppTasks();
            } catch (Exception ignore) {
            }

            if (list != null) {
                for (ActivityManager.AppTask t : list) {
                    t.finishAndRemoveTask();
                }
            }
        }

        // 传递给保存的UncaughtExceptionHandler
        mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }

    private static class CrashHandlerHolder {

        static final CrashHandler INSTANCE = new CrashHandler();
    }

    private CrashHandler() {
    }
}
