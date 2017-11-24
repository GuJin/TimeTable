package com.sunrain.timetablev4.utils;

import com.sunrain.timetablev4.application.MyApplication;

public class DensityUtil {

    private static float sDensity;

    static {
        sDensity = MyApplication.sContext.getResources().getDisplayMetrics().density;
    }

    public static int dip2Px(float dpValue) {
        return (int) (dpValue * sDensity + 0.5f);
    }
}
