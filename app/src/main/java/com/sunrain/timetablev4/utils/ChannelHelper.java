package com.sunrain.timetablev4.utils;

import com.meituan.android.walle.WalleChannelReader;
import com.sunrain.timetablev4.BuildConfig;
import com.sunrain.timetablev4.application.MyApplication;

public class ChannelHelper {

    private static String sChannel;

    static {
        sChannel = "test";
        if (BuildConfig.DEBUG) {
            sChannel = "test";
        } else {
            sChannel = WalleChannelReader.getChannel(MyApplication.sContext, "unknown");
        }
    }

    public static String getChannel() {
        return sChannel;
    }

    private ChannelHelper() {
    }
}