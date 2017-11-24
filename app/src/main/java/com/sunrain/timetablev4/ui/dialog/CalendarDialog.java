package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarDialog extends BaseDialog<CalendarDialog> implements CalendarView.OnDateChangeListener {

    private CalendarView mCalendarView;
    private int mYear;
    private int mMonth;
    private int mDayOfMonth;

    public CalendarDialog(@NonNull Context context) {
        super(context);
        setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_calendar, parent, false);
        mCalendarView = view.findViewById(R.id.calendarView);
        mCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        mCalendarView.setOnDateChangeListener(this);
        return view;
    }

    public CalendarDialog setDateRange(long startDate, long endDate) {
        if (startDate != 0) {
            mCalendarView.setMinDate(startDate);
        }
        if (endDate != 0) {
            mCalendarView.setMaxDate(endDate);
        }
        return this;
    }

    public long getDate() {
        if (mYear == 0) {
            // 未选择
            return mCalendarView.getDate();
        }
        return new GregorianCalendar(mYear, mMonth, mDayOfMonth).getTimeInMillis();
    }

    public CalendarDialog setDate(long date) {
        if (date != 0) {
            mCalendarView.setDate(date);
        }
        return this;
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        mYear = year;
        mMonth = month;
        mDayOfMonth = dayOfMonth;
    }
}