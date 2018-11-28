package com.sunrain.timetablev4.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.UserSpinner;
import com.sunrain.timetablev4.view.table.TableData;

public class TableFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, UserSpinner
        .OnItemSelectedByUserListener {

    private UserSpinner mSpMorning;
    private UserSpinner mSpAfternoon;
    private UserSpinner mSpEvening;
    private Switch mSwDoubleWeek;
    private CheckBox mCbSat;
    private CheckBox mCbSun;
    private Integer[] mClassIntegers;
    private Integer[] mEveningClassIntegers;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_table, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mSpMorning = view.findViewById(R.id.sp_morning);
        mSpAfternoon = view.findViewById(R.id.sp_afternoon);
        mSpEvening = view.findViewById(R.id.sp_evening);
        mSwDoubleWeek = view.findViewById(R.id.sw_double_week);
        mCbSat = view.findViewById(R.id.cb_sat);
        mCbSun = view.findViewById(R.id.cb_sun);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initSpinner();
        initSwitch();
        initCheckBox();
        setListener();
    }

    private void initSwitch() {
        int enabled = SharedPreUtils.getInt(SharedPreConstants.DOUBLE_WEEK, SharedPreConstants.DEFAULT_DOUBLE_WEEK);
        mSwDoubleWeek.setChecked(enabled == 1);
    }

    private void initCheckBox() {
        int workday = SharedPreUtils.getInt(SharedPreConstants.WORK_DAY, SharedPreConstants.DEFAULT_WORK_DAY);
        if (workday > 5) {
            mCbSat.setChecked(true);
            mCbSun.setVisibility(View.VISIBLE);
            if (workday == 7) {
                mCbSun.setChecked(true);
            }
        }
    }

    private void initSpinner() {
        //起始周结束周spinner
        mClassIntegers = new Integer[]{2, 3, 4, 5, 6, 7, 8};
        mEveningClassIntegers = new Integer[]{0, 1, 2, 3, 4, 5};

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(mActivity, R.layout.spinner_item, mClassIntegers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<Integer> eveningAdapter = new ArrayAdapter<>(mActivity, R.layout.spinner_item, mEveningClassIntegers);
        eveningAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpMorning.setAdapter(adapter);
        mSpAfternoon.setAdapter(adapter);
        mSpEvening.setAdapter(eveningAdapter);

        //从2开始的，减去2为position
        mSpMorning.setSelection(SharedPreUtils.getInt(SharedPreConstants.MORNING_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_MORNING_CLASS_NUMBER) - 2);
        mSpAfternoon.setSelection(SharedPreUtils.getInt(SharedPreConstants.AFTERNOON_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_AFTERNOON_CLASS_NUMBER) - 2);
        mSpEvening.setSelection(SharedPreUtils.getInt(SharedPreConstants.EVENING_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_EVENING_CLASS_NUMBER));
    }

    private void setListener() {
        mSpMorning.setOnItemSelectedByUserListener(this);
        mSpAfternoon.setOnItemSelectedByUserListener(this);
        mSpEvening.setOnItemSelectedByUserListener(this);

        mSwDoubleWeek.setOnCheckedChangeListener(this);

        mCbSat.setOnCheckedChangeListener(this);
        mCbSun.setOnCheckedChangeListener(this);
    }

    @Override
    public void onItemSelectByUser(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_morning:
                SharedPreUtils.putInt(SharedPreConstants.MORNING_CLASS_NUMBER, mClassIntegers[position]);
                break;
            case R.id.sp_afternoon:
                SharedPreUtils.putInt(SharedPreConstants.AFTERNOON_CLASS_NUMBER, mClassIntegers[position]);
                break;
            case R.id.sp_evening:
                SharedPreUtils.putInt(SharedPreConstants.EVENING_CLASS_NUMBER, mEveningClassIntegers[position]);
                break;
        }
        TableData.getInstance().setLayoutChange();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_double_week:
                SharedPreUtils.putInt(SharedPreConstants.DOUBLE_WEEK, isChecked ? 1 : 0);
                TableData.getInstance().setContentChange();
                break;
            case R.id.cb_sat:
                mCbSun.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                mCbSun.setChecked(false);
                SharedPreUtils.putInt(SharedPreConstants.WORK_DAY, isChecked ? 6 : 5);
                TableData.getInstance().setLayoutChange();
                break;
            case R.id.cb_sun:
                SharedPreUtils.putInt(SharedPreConstants.WORK_DAY, isChecked ? 7 : 6);
                TableData.getInstance().setLayoutChange();
                break;
        }
    }

}
