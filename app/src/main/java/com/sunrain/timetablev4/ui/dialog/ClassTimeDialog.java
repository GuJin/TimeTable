package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.base.BaseDialog;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.constants.TableConstants;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.UserSpinner;


public class ClassTimeDialog extends BaseDialog<ClassTimeDialog> implements UserSpinner.OnItemSelectedByUserListener {

    private final Context mContext;
    private final boolean isDoubleWeekEnabled;

    private UserSpinner mSpWeek;
    private UserSpinner mSpSection;
    private UserSpinner mSpTime;
    private UserSpinner mSpStartWeek;
    private UserSpinner mSpEndWeek;
    private RadioGroup mRgDoubleWeek;

    private ArrayAdapter<String> mMorningClassAdapter;
    private ArrayAdapter<String> mAfternoonClassAdapter;
    private ArrayAdapter<String> mEveningClassAdapter;
    private ClassBean mClassBean;

    public ClassTimeDialog(Context context, boolean isDoubleWeekEnabled) {
        super(context);
        mContext = context;

        this.isDoubleWeekEnabled = isDoubleWeekEnabled;

        if (isDoubleWeekEnabled) {
            mRgDoubleWeek.setVisibility(View.VISIBLE);
        }

        mSpStartWeek.setOnItemSelectedByUserListener(this);
        mSpEndWeek.setOnItemSelectedByUserListener(this);
        mSpSection.setOnItemSelectedByUserListener(this);
        prepareSpinnerData();

        setNegativeButton(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        setRequireWidth(DensityUtil.dip2Px(340));
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_class_time, parent, false);

        mSpWeek = view.findViewById(R.id.sp_week);
        mSpSection = view.findViewById(R.id.sp_section);
        mSpTime = view.findViewById(R.id.sp_time);
        mSpStartWeek = view.findViewById(R.id.sp_start_week);
        mSpEndWeek = view.findViewById(R.id.sp_end_week);
        mRgDoubleWeek = view.findViewById(R.id.rg_double_week);

        return view;
    }

    public void setClassBean(@NonNull ClassBean classBean) {
        mClassBean = classBean;
        refreshSpinner();
        if (isDoubleWeekEnabled) {
            refreshCheckBox();
        }
    }

    private void refreshCheckBox() {
        if (mClassBean.doubleWeek == 1) {
            mRgDoubleWeek.check(R.id.rb_double_week_double);
        } else if (mClassBean.doubleWeek == 2) {
            mRgDoubleWeek.check(R.id.rb_double_week_odd);
        } else {
            mRgDoubleWeek.check(R.id.rb_double_week_all);
        }
    }

    public ClassBean getClassBean() {
        ClassBean classBean = new ClassBean();
        classBean._id = mClassBean._id;
        classBean.course = mClassBean.course;
        classBean.classroom = mClassBean.classroom;
        classBean.week = mSpWeek.getSelectedItemPosition();
        classBean.section = mSpSection.getSelectedItemPosition();
        classBean.time = mSpTime.getSelectedItemPosition();
        classBean.startWeek = mSpStartWeek.getSelectedItemPosition();
        classBean.endWeek = mSpEndWeek.getSelectedItemPosition();
        if (isDoubleWeekEnabled) {
            int id = mRgDoubleWeek.getCheckedRadioButtonId();
            if (id == R.id.rb_double_week_double) {
                classBean.doubleWeek = 1;
            } else if (id == R.id.rb_double_week_odd) {
                classBean.doubleWeek = 2;
            } else {
                classBean.doubleWeek = 0;
            }
        }
        return classBean;
    }

    @Override
    public void onItemSelectByUser(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_start_week:
                mClassBean.startWeek = position;
                if (position > mSpEndWeek.getSelectedItemPosition()) {
                    mSpEndWeek.setSelection(position);
                }
                break;
            case R.id.sp_end_week:
                mClassBean.endWeek = position;
                if (position < mSpStartWeek.getSelectedItemPosition()) {
                    mSpStartWeek.setSelection(position);
                }
                break;
            case R.id.sp_section:
                if (position == TableConstants.MORNING) {
                    mSpTime.setAdapter(mMorningClassAdapter);
                } else if (position == TableConstants.AFTERNOON) {
                    mSpTime.setAdapter(mAfternoonClassAdapter);
                } else {
                    mSpTime.setAdapter(mEveningClassAdapter);
                }
                break;
        }
    }

    private void refreshSpinner() {
        // 增加判断是防止修改学期或表格属性后，课程属性数组越界

        int week = mSpWeek.getAdapter().getCount() - 1;
        mSpWeek.setSelection(mClassBean.week > week ? week : mClassBean.week);

        int section = mSpSection.getAdapter().getCount() - 1;
        mSpSection.setSelection(mClassBean.section > section ? section : mClassBean.section);

        if (mSpSection.getSelectedItemPosition() == TableConstants.MORNING) {
            mSpTime.setAdapter(mMorningClassAdapter);
        } else if (mSpSection.getSelectedItemPosition() == TableConstants.AFTERNOON) {
            mSpTime.setAdapter(mAfternoonClassAdapter);
        } else {
            mSpTime.setAdapter(mEveningClassAdapter);
        }
        int time = mSpTime.getAdapter().getCount() - 1;
        mSpTime.setSelection(mClassBean.time > time ? time : mClassBean.time);

        int semesterWeek = mSpEndWeek.getAdapter().getCount() - 1;
        mSpStartWeek.setSelection(mClassBean.startWeek > semesterWeek ? semesterWeek : mClassBean.startWeek);
        mSpEndWeek.setSelection(mClassBean.endWeek > semesterWeek ? semesterWeek : mClassBean.endWeek);
    }

    private void prepareSpinnerData() {
        Resources resources = MyApplication.sContext.getResources();

        String[] tempWeeks = resources.getStringArray(R.array.week);
        String[] tempSections = resources.getStringArray(R.array.section);
        String[] tempTimes = resources.getStringArray(R.array.time);

        String[] weeks;
        String[] sections;

        String[] morningTimes;
        String[] afternoonTimes;
        String[] eveningTimes;

        int workday = SharedPreUtils.getInt(SharedPreConstants.WORK_DAY, 5);
        weeks = new String[workday];
        System.arraycopy(tempWeeks, 0, weeks, 0, workday);
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, weeks);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpWeek.setAdapter(weekAdapter);

        if (SharedPreUtils.getInt(SharedPreConstants.EVENING_CLASS_NUMBER, 0) > 0) {// 有晚上课程
            sections = new String[3];
            System.arraycopy(tempSections, 0, sections, 0, 3);
        } else {
            sections = new String[2];
            System.arraycopy(tempSections, 0, sections, 0, 2);
        }

        ArrayAdapter<String> sectionsAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, sections);
        sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSection.setAdapter(sectionsAdapter);

        int morningClass = SharedPreUtils.getInt(SharedPreConstants.MORNING_CLASS_NUMBER, SharedPreConstants.DEFAULT_MORNING_CLASS_NUMBER);
        morningTimes = new String[morningClass];
        System.arraycopy(tempTimes, 0, morningTimes, 0, morningClass);
        mMorningClassAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, morningTimes);
        mMorningClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int afternoonClass = SharedPreUtils.getInt(SharedPreConstants.AFTERNOON_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_AFTERNOON_CLASS_NUMBER);
        afternoonTimes = new String[afternoonClass];
        System.arraycopy(tempTimes, 0, afternoonTimes, 0, afternoonClass);
        mAfternoonClassAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, afternoonTimes);
        mAfternoonClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int eveningClass = SharedPreUtils.getInt(SharedPreConstants.EVENING_CLASS_NUMBER, SharedPreConstants.DEFAULT_EVENING_CLASS_NUMBER);
        eveningTimes = new String[eveningClass];
        System.arraycopy(tempTimes, 0, eveningTimes, 0, eveningClass);
        mEveningClassAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, eveningTimes);
        mEveningClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        int week = SharedPreUtils.getInt(SharedPreConstants.SEMESTER_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        Integer[] duringWeeks = new Integer[week];
        for (int i = 0; i < week; i++) {
            duringWeeks[i] = i + 1;
        }
        ArrayAdapter<Integer> duringAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, duringWeeks);
        duringAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpStartWeek.setAdapter(duringAdapter);
        mSpEndWeek.setAdapter(duringAdapter);
    }
}
