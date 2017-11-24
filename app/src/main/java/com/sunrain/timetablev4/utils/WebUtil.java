package com.sunrain.timetablev4.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.sunrain.timetablev4.application.MyApplication;

import tech.gujin.toast.ToastUtil;

public class WebUtil {
    public static void gotoWeb(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(MyApplication.sContext.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            ToastUtil.show("未检测到浏览器");
        }
    }
}