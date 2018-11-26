package com.sunrain.timetablev4.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.sunrain.timetablev4.BuildConfig;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.dao.AppWidgetDao;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class DayAppWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_RESTORE = BuildConfig.APPLICATION_ID + ".ACTION_RESTORE";
    private static final String ACTION_YESTERDAY = BuildConfig.APPLICATION_ID + ".ACTION_YESTERDAY";
    private static final String ACTION_TOMORROW = BuildConfig.APPLICATION_ID + ".ACTION_TOMORROW";
    private static final String ACTION_NEW_DAY = BuildConfig.APPLICATION_ID + ".ACTION_NEW_DAY";

    private static final int ONE_DAY_MILLIS = 86400000;

    @Override
    public void onEnabled(Context context) {
        registerNewDayBroadcast();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (isAlarmManagerNotSet()) {
            registerNewDayBroadcast();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M月d日 E", Locale.getDefault());

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, DayAppWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            long currentTimeMillis = System.currentTimeMillis();
            AppWidgetDao.saveAppWidgetCurrentTime(appWidgetId, currentTimeMillis);

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
            rv.setRemoteAdapter(R.id.lv_day_appwidget, intent);
            rv.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
            rv.setTextViewText(R.id.tv_date, simpleDateFormat.format(currentTimeMillis));
            rv.setInt(R.id.fl_root, "setBackgroundColor", AppWidgetDao.getAppWidgetBackgroundColor(appWidgetId, Color
                    .TRANSPARENT));

            rv.setOnClickPendingIntent(R.id.imgBtn_restore, makePendingIntent(context, appWidgetId, ACTION_RESTORE));
            rv.setOnClickPendingIntent(R.id.imgBtn_yesterday, makePendingIntent(context, appWidgetId,
                    ACTION_YESTERDAY));
            rv.setOnClickPendingIntent(R.id.imgBtn_tomorrow, makePendingIntent(context, appWidgetId, ACTION_TOMORROW));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 除了自己的action 还有系统的
        String action = intent.getAction();

        if (ACTION_NEW_DAY.equals(action)) {
            notifyUpdate(context);
            return;
        }

        if (ACTION_RESTORE.equals(action) || ACTION_YESTERDAY.equals(action) || ACTION_TOMORROW.equals(action)) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M月d日 E", Locale.getDefault());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager
                    .INVALID_APPWIDGET_ID);

            long currentTime;
            long newTime;

            if (ACTION_RESTORE.equals(action)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.INVISIBLE);
                newTime = System.currentTimeMillis();
            } else if (ACTION_YESTERDAY.equals(action)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(appWidgetId, System.currentTimeMillis());
                newTime = currentTime - ONE_DAY_MILLIS;
            } else { //ACTION_TOMORROW
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(appWidgetId, System.currentTimeMillis());
                newTime = currentTime + ONE_DAY_MILLIS;
            }

            AppWidgetDao.saveAppWidgetCurrentTime(appWidgetId, newTime);
            rv.setTextViewText(R.id.tv_date, simpleDateFormat.format(newTime));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDao.deleteAppWidget(appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        unregisterNewDayBroadcast();
        AppWidgetDao.clear();
    }

    private PendingIntent makePendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void updateAppWidgetConfig(AppWidgetManager appWidgetManager, int appWidgetId, int color, int timeStyle) {
        AppWidgetDao.saveAppWidgetConfig(appWidgetId, color, timeStyle);

        Intent intent = new Intent(MyApplication.sContext, DayAppWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(MyApplication.sContext.getPackageName(), R.layout.day_appwidget);
        views.setRemoteAdapter(R.id.lv_day_appwidget, intent);
        views.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
        views.setInt(R.id.fl_root, "setBackgroundColor", color);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    public static void noticeAppWidgetViewDataChanger() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyApplication.sContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MyApplication.sContext,
                DayAppWidgetProvider.class));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_day_appwidget);
    }

    public void notifyUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MyApplication.sContext,
                DayAppWidgetProvider.class));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void registerNewDayBroadcast() {
        AlarmManager alarmManager = (AlarmManager) MyApplication.sContext.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(MyApplication.sContext, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.sContext, 0, intent, 0);

        Calendar midnight = Calendar.getInstance(Locale.getDefault());
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 1); // 只是为了确保肯定在明天
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1); // 设置为明天

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), ONE_DAY_MILLIS, pendingIntent);
    }

    private void unregisterNewDayBroadcast() {
        AlarmManager alarmManager = (AlarmManager) MyApplication.sContext.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(MyApplication.sContext, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.sContext, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private boolean isAlarmManagerNotSet() {
        Intent intent = new Intent(MyApplication.sContext, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        return PendingIntent.getBroadcast(MyApplication.sContext, 0, intent, PendingIntent.FLAG_NO_CREATE) == null;
    }

}