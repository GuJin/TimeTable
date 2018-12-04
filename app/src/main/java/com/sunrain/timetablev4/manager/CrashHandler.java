package com.sunrain.timetablev4.manager;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.os.TransactionTooLargeException;
import android.widget.Toast;

import com.sunrain.timetablev4.application.MyApplication;

import java.io.File;
import java.util.List;

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
            if(ex instanceof TransactionTooLargeException || ex.getCause() instanceof TransactionTooLargeException){

                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(MyApplication.sContext, "崩溃了，请邮件向itimetable@foxmail.com反馈", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();

                // 目前尚未知道TransactionTooLargeException崩溃原因，怀疑是壁纸过大
                File picFile = new File(MyApplication.sContext.getFilesDir(), WallpaperManager.FILE_NAME);
                if (picFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    picFile.delete();
                }
                // 为了让进程多存活一会，让Toast显示
                SystemClock.sleep(2500);
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
