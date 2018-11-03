package com.sunrain.timetablev4.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppWidgetDao extends BaseDao {

    private static final String TABLE_NAME = "app_widget";

    public static void saveAppWidgetBackgroundColor(int appWidgetId, int backgroundColor) {
        SQLiteDatabase database = DBManager.getDb();
        ContentValues values = new ContentValues(2);
        values.put("appWidgetId", appWidgetId);
        values.put("backgroundColor", backgroundColor);
        insertOrReplace(database, TABLE_NAME, values);
        DBManager.close(database);
    }

    public static int getAppWidgetBackgroundColor(int appWidgetId, int defaultColor) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"backgroundColor"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultColor;
        }

        int backgroundColorIndex = cursor.getColumnIndex("backgroundColor");
        int backgroundColor;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            backgroundColor = cursor.getInt(backgroundColorIndex);
        } else {
            backgroundColor = defaultColor;
        }

        cursor.close();

        return backgroundColor;
    }

    public static void saveAppWidgetCurrentTime(int appWidgetId, long currentTime) {
        SQLiteDatabase database = DBManager.getDb();
        ContentValues values = new ContentValues(2);
        values.put("appWidgetId", appWidgetId);
        values.put("currentTime", currentTime);
        insertOrReplace(database, TABLE_NAME, values);
        DBManager.close(database);
    }

    public static long getAppWidgetCurrentTime(int appWidgetId, long defaultTime) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"currentTime"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultTime;
        }

        int currentTimeIndex = cursor.getColumnIndex("currentTime");
        long currentTime;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            currentTime = cursor.getLong(currentTimeIndex);
        } else {
            currentTime = defaultTime;
        }

        cursor.close();

        return currentTime;
    }

    public static void deleteAppWidget(int appWidgetId) {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, "appWidgetId = ?", new String[]{String.valueOf(appWidgetId)});
        DBManager.close(db);
    }

    public static void clear() {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, null, null);
        DBManager.close(db);
    }
}
