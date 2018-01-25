package com.sunrain.timetablev4.utils;

import com.sunrain.timetablev4.BuildConfig;

public class ChannelHelper {

    private static String sChannel;

    static {
        sChannel = "test";
        if (BuildConfig.DEBUG) {
            sChannel = "test";
        } else {
            //sChannel = WalleChannelReader.getChannel(MyApplication.sContext, "unknown");
        }
    }

    public static String getChannel() {
        return sChannel;
    }

    private ChannelHelper() {
    }
}