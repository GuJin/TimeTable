package com.sunrain.timetablev4.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.dialog.CalendarDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.UserSpinner;
import com.sunrain.timetablev4.view.table.TableData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tech.gujin.toast.ToastUtil;

public class SemesterFragment extends BaseFragment implements View.OnClickListener, UserSpinner.OnItemSelectedByUserListener {

    private static final int SAVE_SUCCEED = 1;
    private static final int ERROR_MORE_THAN_60 = -2;

    private CalendarDialog mStartCalendarDialog;
    private CalendarDialog mEndCalendarDialog;
    private TextView mTvStart;
    private TextView mTvEnd;
    private UserSpinner mSpWeek;

    private int mWeek;
    private long mStartDate;
    private long mEndDate;
    private boolean isEndWeekRefresh;
    private SimpleDateFormat mSimpleDateFormat;
    private boolean isWeekChanged;


    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_semester, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mTvStart = view.findViewById(R.id.tv_start);
        mTvEnd = view.findViewById(R.id.tv_end);
        mSpWeek = view.findViewById(R.id.sp_week);
    }

    @Override
    public void initData() {
        mSimpleDateFormat = new SimpleDateFormat("yyyy - MM - dd", Locale.getDefault());
        initSpinner();
        setDate();
        setListener();
    }

    private void initSpinner() {
        Integer[] weeksArray = new Integer[60];
        for (int i = 0; i < 60; i++) {
            weeksArray[i] = i + 1;
        }
        ArrayAdapter<Integer> weekAdapter = new ArrayAdapter<>(mActivity, R.layout.spinner_item, weeksArray);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpWeek.setAdapter(weekAdapter);
        mWeek = SharedPreUtils.getInt(SharedPreConstants.SEMESTER_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        mSpWeek.setSelection(mWeek - 1);
    }

    private void setDate() {
        mStartDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        mEndDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);

        if (mStartDate != 0) {
            mTvStart.setText(mSimpleDateFormat.format(new Date(mStartDate)));
        }
        if (mEndDate != 0) {
            mTvEnd.setText(mSimpleDateFormat.format(new Date(mEndDate)));
        }
    }

    private void setListener() {
        mTvStart.setOnClickListener(this);
        mTvEnd.setOnClickListener(this);
        mSpWeek.setOnItemSelectedByUserListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_start:
                showStartDatePickerDialog(mStartDate);
                break;
            case R.id.tv_end:
                if (mStartDate == 0) {
                    ToastUtil.show("请先设置起始日期");
                } else {
                    showEndDatePickerDialog(mEndDate);
                }
                break;
        }
    }

    private void showStartDatePickerDialog(long date) {
        if (mStartCalendarDialog == null) {
            initStartDialog();
        }

        if (date > 0) {
            mStartCalendarDialog.setDate(date);
        }
        mStartCalendarDialog.show();
    }

    private void initStartDialog() {
        mStartCalendarDialog = new CalendarDialog(mActivity).setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long calendarDate = mStartCalendarDialog.getDate();
                if (calendarDate == mStartDate) {
                    dialog.dismiss();
                    return;
                }

                mStartDate = calendarDate;

                Date startDate = new Date(mStartDate);
                mTvStart.setText(mSimpleDateFormat.format(startDate));
                SharedPreUtils.putLong(SharedPreConstants.SEMESTER_START_DATE, mStartDate);
                TableData.getInstance().setContentChange();

                //如果设置了周数，则按照周数计算结束日期
                if (mWeek != 0) {
                    calculateEndDate(startDate);
                    dialog.dismiss();
                }
            }
        });
    }

    private void showEndDatePickerDialog(long date) {
        if (mEndCalendarDialog == null) {
            initEndDialog();
        }

        if (date > 0) {
            mEndCalendarDialog.setDate(date);
        }

        mEndCalendarDialog.show();
    }

    private void initEndDialog() {
        mEndCalendarDialog = new CalendarDialog(mActivity).setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                long calendarDate = mEndCalendarDialog.getDate();
                if (calendarDate == mEndDate) {
                    dialog.dismiss();
                    return;
                }

                if (calendarDate < mStartDate) {
                    ToastUtil.show("结束日期不能小于起始日期");
                    return;
                }

                if (refreshWeek(mStartDate, calendarDate) == SAVE_SUCCEED) {
                    mEndDate = calendarDate;
                    mTvEnd.setText(mSimpleDateFormat.format(new Date(calendarDate)));
                    SharedPreUtils.putLong(SharedPreConstants.SEMESTER_END_DATE, mEndDate);
                    TableData.getInstance().setContentChange();
                    dialog.dismiss();
                } else {
                    ToastUtil.show("周数超过60,请减小结束日期");
                }
            }
        });
    }


    private int refreshWeek(long startDate, long endDate) {
        Calendar cl = Calendar.getInstance(Locale.getDefault());
        cl.setFirstDayOfWeek(Calendar.MONDAY);
        cl.setTimeInMillis(startDate);
        int startWeekOfYear = cl.get(Calendar.WEEK_OF_YEAR);
        int startYear = cl.get(Calendar.YEAR);
        //当日在年尾，并且当日周数被计算在下一年的情况处理
        cl.add(Calendar.DAY_OF_YEAR, -7);
        if (cl.get(Calendar.YEAR) == startYear && startWeekOfYear < cl.get(Calendar.WEEK_OF_YEAR)) {
            startYear += 1;
        }
        cl.setTimeInMillis(endDate);
        int endWeekOfYear = cl.get(Calendar.WEEK_OF_YEAR);
        int endYear = cl.get(Calendar.YEAR);
        //当日在年尾，并且当日周数被计算在下一年的情况处理
        cl.add(Calendar.DAY_OF_YEAR, -7);
        if (cl.get(Calendar.YEAR) == endYear && endWeekOfYear < cl.get(Calendar.WEEK_OF_YEAR)) {
            endYear += 1;
        }
        int weeks;
        if (startYear == endYear) {
            weeks = endWeekOfYear - startWeekOfYear + 1;
        } else {
            weeks = (endYear - startYear) * 53 - startWeekOfYear + endWeekOfYear;
        }
        if (weeks > 60) {
            return ERROR_MORE_THAN_60;
        }
        mWeek = weeks;
        isEndWeekRefresh = true;
        mSpWeek.setSelection(mWeek - 1, true);
        saveWeek();
        return SAVE_SUCCEED;
    }

    private void saveWeek() {
        SharedPreUtils.putInt(SharedPreConstants.SEMESTER_WEEK, mWeek);
        isWeekChanged = true;
    }

    private void calculateEndDate(long startDate) {
        if (startDate != 0) {
            calculateEndDate(new Date(startDate));
        }
    }

    private void calculateEndDate(Date startDate) {
        Calendar cl = Calendar.getInstance(Locale.getDefault());
        cl.setFirstDayOfWeek(Calendar.MONDAY);
        cl.setTime(startDate);
        cl.add(Calendar.WEEK_OF_YEAR, mWeek - 1);
        int dayOfWeek = cl.get(Calendar.DAY_OF_WEEK);
        if (cl.getFirstDayOfWeek() == Calendar.MONDAY) {
            cl.add(Calendar.DAY_OF_WEEK, 8 - dayOfWeek);
        } else {
            cl.add(Calendar.DAY_OF_WEEK, 7 - dayOfWeek);
        }

        mEndDate = cl.getTimeInMillis();

        mTvEnd.setText(mSimpleDateFormat.format(cl.getTime()));
        SharedPreUtils.putLong(SharedPreConstants.SEMESTER_END_DATE, mEndDate);
        TableData.getInstance().setContentChange();
    }

    @Override
    public void onItemSelectByUser(AdapterView<?> parent, View view, int position, long id) {
        mWeek = position + 1;
        saveWeek();
        if (isEndWeekRefresh) {
            isEndWeekRefresh = false;
            return;
        }
        calculateEndDate(mStartDate);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            return;
        }

        if (isWeekChanged) {
            isWeekChanged = false;
            if (TableDao.existsOutOfWeek(mWeek - 1)) {
                showOutOfWeekDialog(mWeek);
            }
        }
    }

    private void showOutOfWeekDialog(int week) {
        new MessageDialog(mActivity).setMessage("当前学期总周数为" + week + "周，存在上课时间超出" + week + "周的课程，请注意处理。")
                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .hideNegativeButton()
                .show();
    }
}