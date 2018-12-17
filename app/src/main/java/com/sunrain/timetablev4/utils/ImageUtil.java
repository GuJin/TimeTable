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

//    /**
//     * 得到bitmap的YUV数据
//     *
//     * @param sourceBmp 原始bitmap
//     * @return yuv数据
//     */
//    public static byte[] getBitmapYUVBytes(Bitmap sourceBmp) {
//        if (sourceBmp == null) {
//            return null;
//        }
//        int inputWidth = sourceBmp.getWidth();
//        int inputHeight = sourceBmp.getHeight();
//        int[] argb = new int[inputWidth * inputHeight];
//        sourceBmp.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
//        byte[] yuv = new byte[inputWidth * inputHeight + ((inputWidth % 2 == 0 ? inputWidth : (inputWidth + 1)) * (inputHeight % 2 == 0 ?
//                inputHeight : (inputHeight + 1))) / 2];
//        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
//        sourceBmp.recycle();
//        return yuv;
//    }
//
//    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
//        // 帧图片的像素大小
//        final int frameSize = width * height;
//        // Y的index从0开始
//        int yIndex = 0;
//        // UV的index从frameSize开始
//        int uvIndex = frameSize;
//        // YUV数据, ARGB数据
//        int Y, U, V, a, R, G, B;
//
//        int argbIndex = 0;
//        // ---循环所有像素点，RGB转YUV---
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//
//                // a is not used obviously
//                a = (argb[argbIndex] & 0xff000000) >> 24;
//                R = (argb[argbIndex] & 0xff0000) >> 16;
//                G = (argb[argbIndex] & 0xff00) >> 8;
//                B = (argb[argbIndex] & 0xff);
//                argbIndex++;
//
//                // well known RGB to YUV algorithm
//                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
//                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
//                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
//
//                Y = Math.max(0, Math.min(Y, 255));
//                U = Math.max(0, Math.min(U, 255));
//                V = Math.max(0, Math.min(V, 255));
//
//                // NV21 has a plane of Y and interleaved planes of VU each
//                // sampled by a factor of 2
//                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the
//                // sampling is every other
//                // pixel AND every other scanline.
//                // ---Y---
//                yuv420sp[yIndex++] = (byte) Y;
//                // ---UV---
//                if ((j % 2 == 0) && (i % 2 == 0)) {
//                    yuv420sp[uvIndex++] = (byte) V;
//                    yuv420sp[uvIndex++] = (byte) U;
//                }
//            }
//        }
//    }


    private ImageUtil() {
    }
}
