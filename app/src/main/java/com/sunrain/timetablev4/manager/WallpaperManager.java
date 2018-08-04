package com.sunrain.timetablev4.manager;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.utils.RunnableExecutorService;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import tech.gujin.toast.ToastUtil;

public class WallpaperManager {

    public static final String FILE_NAME = "bg.webp";

    public void refreshWallpaperInBackground(Activity activity) {
        final WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        RunnableExecutorService.when(new Callable<Drawable>() {
            @Override
            public Drawable call() {
                File picFile = new File(MyApplication.sContext.getFilesDir(), FILE_NAME);
                Resources resources = MyApplication.sContext.getResources();
                Bitmap bitmap;
                if (picFile.exists()) {
                    bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                } else {
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_wallpaper_default);
                }
                return new BitmapDrawable(resources, bitmap);
            }
        }).done(new RunnableExecutorService.DoneCallback<Drawable>() {
            @Override
            public void done(Drawable drawable) {
                Activity weakActivity = activityWeakReference.get();
                if (weakActivity == null) {
                    return;
                }
                weakActivity.getWindow().setBackgroundDrawable(drawable);
            }
        }).fail(new RunnableExecutorService.FailCallback() {
            @Override
            public void fail(Throwable throwable) {
                ToastUtil.show("壁纸加载失败");
            }
        }).execute();
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
