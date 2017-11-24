package com.sunrain.timetablev4.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tech.gujin.toast.ToastUtil;

public class ImageUtil {

    public static void saveBitmapToPicturesDirectory(String fileName, Bitmap bitmap) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), File.separator + "我是课程表");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ToastUtil.show("保存失败");
                return;
            }
        }
        File filePic = new File(file, File.separator + fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
        } catch (IOException | IllegalStateException e) {
            ToastUtil.show("保存失败");
            return;
        } finally {
            FileUtil.close(fileOutputStream);
        }
        ToastUtil.show("已保存到" + filePic.getPath());
        //通知相册刷新图片
        MediaScannerConnection.scanFile(MyApplication.sContext, new String[]{filePic.toString()}, null, null);
    }

    public static Bitmap bitmapAddDark(Bitmap bitmap) {
        //不能直接修改资源文件，所以要copy
        Bitmap bitmapDark = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmapDark);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(MyApplication.sContext, R.color.background_dark),
                PorterDuff.Mode.DARKEN));
        canvas.drawBitmap(bitmapDark, 0, 0, paint);
        bitmap.recycle();
        return bitmapDark;
    }

    private ImageUtil() {
    }
}
