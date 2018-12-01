package com.sunrain.timetablev4.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.sunrain.timetablev4.application.MyApplication;


public class ClipboardUtil {

    public static boolean writeToClipboard(CharSequence label, String msg) {
        ClipboardManager clipboardManager = (ClipboardManager) MyApplication.sContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return false;
        }

        ClipData clipData = ClipData.newPlainText(label, msg);
        clipboardManager.setPrimaryClip(clipData);
        return true;
    }

    private ClipboardUtil() {
    }
}
