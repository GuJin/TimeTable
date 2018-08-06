package com.sunrain.timetablev4.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;

public class DayAppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DayAppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class DayAppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private final int mAppWidgetId;

    private int mMorningClasses;
    private int mAfternoonClasses;
    private int mEveningClasses;
    private int mClassCount;
    private SparseArray<ClassBean> mSparseArray;
    private int mDayOfWeek;

    DayAppWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mSparseArray = new SparseArray<>();
    }

    @Override
    public void onDataSetChanged() {

        mMorningClasses = SharedPreUtils.getInt(SharedPreConstants.MORNING_CLASS_NUMBER, SharedPreConstants.DEFAULT_MORNING_CLASS_NUMBER);
        mAfternoonClasses = SharedPreUtils.getInt(SharedPreConstants.AFTERNOON_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_AFTERNOON_CLASS_NUMBER);
        mEveningClasses = SharedPreUtils.getInt(SharedPreConstants.EVENING_CLASS_NUMBER, SharedPreConstants.DEFAULT_EVENING_CLASS_NUMBER);
        mClassCount = mMorningClasses + mAfternoonClasses + mEveningClasses;

        long currentTime = SharedPreUtils.getLong(SharedPreConstants.APPWIDGET_CURRENT_TIME_1, System.currentTimeMillis());
        mDayOfWeek = CalendarUtil.getDayOfWeek(currentTime);

        mSparseArray.clear();
        SparseArray<ClassBean> classesInDay = TableDao.getClassesInDay(CalendarUtil.getCurrentWeek(currentTime), mDayOfWeek);

        for (int i = 0; i < classesInDay.size(); i++) {
            int key = classesInDay.keyAt(i);
            mSparseArray.put(key, classesInDay.get(key));
        }
    }

    @Override
    public void onDestroy() {
        mSparseArray.clear();
        mClassCount = 0;
    }

    @Override
    public int getCount() {
        return mSparseArray.size() == 0 ? 0 : mClassCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_day_appwidget);

        int key = mDayOfWeek * 100;
        int time;
        String sectionTime;
        if (position < mMorningClasses) {
            // 上午
            sectionTime = ClassBean.Format.getFormatTimeInDay(0, position);
            key += position;
        } else if (position < mMorningClasses + mAfternoonClasses) {
            // 下午
            time = position - mMorningClasses;
            sectionTime = ClassBean.Format.getFormatTimeInDay(1, time);
            key += (10 + time);
        } else {
            // 晚上
            time = position - mMorningClasses - mAfternoonClasses;
            sectionTime = ClassBean.Format.getFormatTimeInDay(2, time);
            key += (20 + time);
        }

        ClassBean classBean = mSparseArray.get(key);
        rv.setTextViewText(R.id.tv_time, sectionTime);

        if (classBean != null) {
            rv.setTextViewText(R.id.tv_course, classBean.course);
            rv.setTextViewText(R.id.tv_classroom, classBean.classroom);
        } else {
            rv.setTextViewText(R.id.tv_course, null);
            rv.setTextViewText(R.id.tv_classroom, null);
        }
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}