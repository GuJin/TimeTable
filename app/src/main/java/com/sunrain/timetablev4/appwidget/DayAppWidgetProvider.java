package com.sunrain.timetablev4.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DayAppWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_RESTORE = "ACTION_RESTORE";

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

            Intent restoreIntent = new Intent(context, DayAppWidgetProvider.class);
            restoreIntent.setAction(ACTION_RESTORE);
            restoreIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent restorePendingIntent = PendingIntent.getBroadcast(context, 0, restoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.imgBtn_restore, restorePendingIntent);
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        String action = intent.getAction();
        if (ACTION_RESTORE.equals(action)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(AppWidgetManager appWidgetManager, int appWidgetId, int color) {
        RemoteViews views = new RemoteViews(MyApplication.sContext.getPackageName(), R.layout.day_appwidget);
        views.setInt(R.id.fl_root, "setBackgroundColor", color);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }
}
