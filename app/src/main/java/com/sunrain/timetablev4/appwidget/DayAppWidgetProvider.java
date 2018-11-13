package com.sunrain.timetablev4.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.dao.AppWidgetDao;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DayAppWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_RESTORE = "ACTION_RESTORE";
    private static final String ACTION_YESTERDAY = "ACTION_YESTERDAY";
    private static final String ACTION_TOMORROW = "ACTION_TOMORROW";

    private static final int ONE_DAY_MILLIS = 86400000;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M月d日 E", Locale.getDefault());

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, DayAppWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
            rv.setRemoteAdapter(R.id.lv_day_appwidget, intent);
            rv.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
            rv.setTextViewText(R.id.tv_date, simpleDateFormat.format(System.currentTimeMillis()));
            rv.setInt(R.id.fl_root, "setBackgroundColor", AppWidgetDao.getAppWidgetBackgroundColor(appWidgetId, Color.TRANSPARENT));

            rv.setOnClickPendingIntent(R.id.imgBtn_restore, makePendingIntent(context, appWidgetId, ACTION_RESTORE));
            rv.setOnClickPendingIntent(R.id.imgBtn_yesterday, makePendingIntent(context, appWidgetId, ACTION_YESTERDAY));
            rv.setOnClickPendingIntent(R.id.imgBtn_tomorrow, makePendingIntent(context, appWidgetId, ACTION_TOMORROW));

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 除了自己的action 还有系统的
        String action = intent.getAction();

        if (ACTION_RESTORE.equals(action) || ACTION_YESTERDAY.equals(action) || ACTION_TOMORROW.equals(action)) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M月d日 E", Locale.getDefault());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_appwidget);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

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

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDao.deleteAppWidget(appWidgetId);
        }
    }

    private PendingIntent makePendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void updateAppWidgetBackground(AppWidgetManager appWidgetManager, int appWidgetId, int color) {
        AppWidgetDao.saveAppWidgetBackgroundColor(appWidgetId, color);
        RemoteViews views = new RemoteViews(MyApplication.sContext.getPackageName(), R.layout.day_appwidget);
        views.setInt(R.id.fl_root, "setBackgroundColor", color);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    public static void noticeAppWidgetUpdate() {
        Intent intent = new Intent(MyApplication.sContext, DayAppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyApplication.sContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MyApplication.sContext, DayAppWidgetProvider.class));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_day_appwidget);
        //        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        //        MyApplication.sContext.sendBroadcast(intent);
    }


}
