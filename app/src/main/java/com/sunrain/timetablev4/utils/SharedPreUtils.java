package com.sunrain.timetablev4.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sunrain.timetablev4.application.MyApplication;

public class SharedPreUtils {

    private static SharedPreferences.Editor sEdit;
    private static SharedPreferences sSharedPreferences;

    static {
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.sContext);
        sEdit = sSharedPreferences.edit();
    }

    public static void putString(String str, String content) {
        sEdit.putString(str, content).apply();
    }

    public static void putInt(String str, int content) {
        sEdit.putInt(str, content).apply();
    }

    public static void putFloat(String str, double content) {
        sEdit.putFloat(str, (float) content).apply();
    }

    public static void putLong(String str, long content) {
        sEdit.putLong(str, content).apply();
    }

    public static void putBoolean(String str, Boolean content) {
        sEdit.putBoolean(str, content).apply();
    }

    public static boolean getBoolean(String str, boolean defaultValue) {
        return sSharedPreferences.getBoolean(str, defaultValue);
    }

    public static String getString(String str, String defaultValue) {
        return sSharedPreferences.getString(str, defaultValue);
    }

    public static int getInt(String str, int defaultValue) {
        return sSharedPreferences.getInt(str, defaultValue);
    }

    public static double getDouble(String str, double defaultValue) {
        return sSharedPreferences.getFloat(str, (float) defaultValue);
    }

    public static long getLong(String str, long defaultValue) {
        return sSharedPreferences.getLong(str, defaultValue);
    }


    public static void remove(String str) {
        sEdit.remove(str).apply();
    }

    public static void clearData() {
        sEdit.clear().apply();
    }

}
