package com.sunrain.timetablev4.ui.fragment;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    private RadioGroup rgSettings;
    private FragmentChanger mFragmentChanger;
    private SparseArray<Class<? extends Fragment>> mFragmentArray;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        rgSettings = view.findViewById(R.id.rg_settings);
    }

    @Override
    public void initData() {
        setListener();
        initFragment();
        mFragmentChanger = new FragmentChanger(getChildFragmentManager(), R.id.fl_content);
        mFragmentChanger.showFragment(SemesterFragment.class);
    }

    private void initFragment() {
        mFragmentArray = new SparseArray<>();
        mFragmentArray.put(R.id.rb_semester, SemesterFragment.class);
        mFragmentArray.put(R.id.rb_table, TableFragment.class);
        mFragmentArray.put(R.id.rb_background, CourseManagementFragment.class);
        mFragmentArray.put(R.id.rb_more, MoreFragment.class);
    }

    private void setListener() {
        rgSettings.setOnCheckedChangeListener(this);
        int childCount = rgSettings.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((RadioButton) rgSettings.getChildAt(i)).setOnCheckedChangeListener(this);
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
}
