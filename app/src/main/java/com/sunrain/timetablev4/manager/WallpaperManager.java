package com.sunrain.timetablev4.manager;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;

import java.io.File;
import java.lang.ref.WeakReference;

import tech.gujin.toast.ToastUtil;

public class WallpaperManager {

    public static final String FILE_NAME = "bg.webp";

    public void refreshWallpaperInBackground(Activity activity) {
        final WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Drawable drawable;
                File picFile = new File(MyApplication.sContext.getFilesDir(), FILE_NAME);
                Resources resources = MyApplication.sContext.getResources();

                if (!picFile.exists()) {
                    drawable = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.bg_wallpaper_default));
                } else {
                    drawable = new BitmapDrawable(resources, BitmapFactory.decodeFile(picFile.getAbsolutePath()));
                }

                final Activity weakActivity = activityWeakReference.get();
                if (weakActivity == null) {
                    ToastUtil.postShow("壁纸加载失败");
                    return;
                }

                weakActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weakActivity.getWindow().setBackgroundDrawable(drawable);
                    }
                });

            }
        }).start();
    }

    public static WallpaperManager getInstance() {
        return BackgroundManagerHolder.sInstance;
    }

    private WallpaperManager() {
    }

    private static class BackgroundManagerHolder {
        private static final WallpaperManager sInstance = new WallpaperManager();
    }
}
