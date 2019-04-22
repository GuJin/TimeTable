package com.sunrain.timetablev4.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.view.table.TableData;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.manager.FragmentChanger;
import com.sunrain.timetablev4.ui.fragment.settings.MoreFragment;
import com.sunrain.timetablev4.ui.fragment.settings.CourseManagementFragment;
import com.sunrain.timetablev4.ui.fragment.settings.SemesterFragment;
import com.sunrain.timetablev4.ui.fragment.settings.TableFragment;

public class SettingsFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private RadioGroup mRgSettings;
    private RadioButton mRbSemester;
    private FragmentChanger mFragmentChanger;
    private SparseArray<Class<? extends Fragment>> mFragmentArray;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mRgSettings = view.findViewById(R.id.rg_settings);
        mRbSemester = view.findViewById(R.id.rb_semester);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        setListener();
        initFragmentArray();

        mFragmentChanger = new FragmentChanger(getChildFragmentManager(), R.id.fl_content);

        if (savedInstanceState != null) {
            mFragmentChanger.onRestoreInstanceState(savedInstanceState);
        } else {
            // 不使用RadioGroup的check方法，会多次调用onCheckedChanged
            // https://stackoverflow.com/questions/10263778/radiogroup-calls-oncheckchanged-three-times
            mRbSemester.setChecked(true);
        }
    }

    private void initFragmentArray() {
        mFragmentArray = new SparseArray<>();
        mFragmentArray.put(R.id.rb_semester, SemesterFragment.class);
        mFragmentArray.put(R.id.rb_table, TableFragment.class);
        mFragmentArray.put(R.id.rb_course_management, CourseManagementFragment.class);
        mFragmentArray.put(R.id.rb_more, MoreFragment.class);
    }

    private void setListener() {
        mRgSettings.setOnCheckedChangeListener(this);
        int childCount = mRgSettings.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((RadioButton) mRgSettings.getChildAt(i)).setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mFragmentChanger.showFragment(mFragmentArray.get(checkedId));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            return;
        }
        TableData.getInstance().refreshDataIfNeed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mFragmentChanger.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
