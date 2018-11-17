package com.sunrain.timetablev4.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppWidgetDao extends BaseDao {

    private static final String TABLE_NAME = "app_widget";

    public static void saveAppWidgetBackgroundColor(int appWidgetId, int backgroundColor) {
        SQLiteDatabase db = DBManager.getDb();

        ContentValues values = new ContentValues(2);
        values.put("backgroundColor", backgroundColor);

        String whereClause = "appWidgetId = ?";
        String[] whereArgs = {String.valueOf(appWidgetId)};

        int number = update(db, TABLE_NAME, values, whereClause, whereArgs);

        if (number == 0) {
            // 使用insertOrReplace会重置其他列的数据
            values.put("appWidgetId", appWidgetId);
            insert(db, TABLE_NAME, values);
        }

        DBManager.close(db);
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

    public static void saveAppWidgetConfig(int appWidgetId, int backgroundColor, int timeStyle) {
        SQLiteDatabase db = DBManager.getDb();

        ContentValues values = new ContentValues(3);
        values.put("backgroundColor", backgroundColor);
        values.put("timeStyle", timeStyle);

        String whereClause = "appWidgetId = ?";
        String[] whereArgs = {String.valueOf(appWidgetId)};

        int number = update(db, TABLE_NAME, values, whereClause, whereArgs);

        if (number == 0) {
            // 使用insertOrReplace会重置其他列的数据
            values.put("appWidgetId", appWidgetId);
            insert(db, TABLE_NAME, values);
        }

        DBManager.close(db);
    }

    public static int getAppWidgetTimeStyle(int appWidgetId, int defaultTimeStyle) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"timeStyle"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultTimeStyle;
        }

        int timeStyleIndex = cursor.getColumnIndex("timeStyle");
        int timeStyle;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            timeStyle = cursor.getInt(timeStyleIndex);
        } else {
            timeStyle = defaultTimeStyle;
        }

        cursor.close();

        return timeStyle;
    }

    public static void saveAppWidgetCurrentTime(int appWidgetId, long currentTime) {
        SQLiteDatabase db = DBManager.getDb();

        ContentValues values = new ContentValues(2);
        values.put("currentTime", currentTime);

        String whereClause = "appWidgetId = ?";
        String[] whereArgs = {String.valueOf(appWidgetId)};

        int number = update(db, TABLE_NAME, values, whereClause, whereArgs);

        if (number == 0) {
            // 使用insertOrReplace会重置其他列的数据
            values.put("appWidgetId", appWidgetId);
            insert(db, TABLE_NAME, values);
        }

        DBManager.close(db);
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
        long currentTime = 0;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            currentTime = cursor.getLong(currentTimeIndex);
        }

        if (currentTime == 0) {
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
