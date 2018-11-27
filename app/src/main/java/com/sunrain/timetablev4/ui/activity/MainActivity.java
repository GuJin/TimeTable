package com.sunrain.timetablev4.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;

import com.sunrain.timetablev4.BuildConfig;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.base.BaseActivity;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.manager.FragmentChanger;
import com.sunrain.timetablev4.manager.WallpaperManager;
import com.sunrain.timetablev4.thread.DataCheckThread;
import com.sunrain.timetablev4.ui.fragment.CourseFragment;
import com.sunrain.timetablev4.ui.fragment.SettingsFragment;
import com.sunrain.timetablev4.utils.ChannelHelper;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.DrawerArrowDrawable;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private FragmentChanger mFragmentChanger;

    private ImageButton mImgBtnSettings;
    private DrawerArrowDrawable mArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFragmentChanger = new FragmentChanger(getFragmentManager(), R.id.fl_main);
            mFragmentChanger.onRestoreInstanceState(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mImgBtnSettings = findViewById(R.id.imgBtn_settings);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        initBugly();
        initFragment();
        initPost(savedInstanceState);
    }

    private void initPost(final Bundle savedInstanceState) {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                initArrow(savedInstanceState);
                setListener();
                setBackground();
                int lastVersionCode = SharedPreUtils.getInt(SharedPreConstants.VERSION_CODE, 0);
                new DataCheckThread(MainActivity.this, lastVersionCode).start();
                if (lastVersionCode != BuildConfig.VERSION_CODE) {
                    SharedPreUtils.putInt(SharedPreConstants.VERSION_CODE, BuildConfig.VERSION_CODE);
                }
            }
        });
    }

    private void setBackground() {
        WallpaperManager.getInstance().refreshWallpaperInBackground(this);
    }

    private void initBugly() {
        if (TextUtils.isEmpty(BuildConfig.BUGLY_ID)) {
            return;
        }
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(MyApplication.sContext);
        strategy.setAppVersion(BuildConfig.VERSION_NAME);
        strategy.setAppPackageName(BuildConfig.APPLICATION_ID);
        strategy.setAppChannel(ChannelHelper.getChannel());
        CrashReport.initCrashReport(MyApplication.sContext, BuildConfig.BUGLY_ID, BuildConfig.DEBUG, strategy);
    }

    private void initFragment() {
        if (mFragmentChanger == null) {
            // 如果mFragmentChanger不为null，则代表在onCreate方法中恢复过，系统会帮助我们显示对应的fragment
            mFragmentChanger = new FragmentChanger(getFragmentManager(), R.id.fl_main);
            mFragmentChanger.showFragment(CourseFragment.class);
        }
    }

    private void setListener() {
        mImgBtnSettings.setOnClickListener(this);
    }

    private void initArrow(Bundle savedInstanceState) {
        mArrow = new DrawerArrowDrawable();
        mArrow.setAnimationListener(new DrawerArrowDrawable.AnimationListener() {
            @Override
            public void onAnimationFinish() {
                mImgBtnSettings.setEnabled(true);
            }
        });
        mImgBtnSettings.setImageDrawable(mArrow);

        // 注意这里的equals条件和changeFragment()方法中的条件相反
        if (savedInstanceState != null && SettingsFragment.class.getSimpleName().equals(mFragmentChanger.getLastFragmentName())) {
            mArrow.startArrowAnim();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_settings:
                mImgBtnSettings.setEnabled(false);
                changeFragment();
                break;
        }
    }

    private void changeFragment() {
        if (CourseFragment.class.getSimpleName().equals(mFragmentChanger.getLastFragmentName())) {
            mFragmentChanger.showFragment(SettingsFragment.class);
            mArrow.startArrowAnim();
        } else {
            mFragmentChanger.showFragment(CourseFragment.class);
            mArrow.startHamburgerAnim();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mFragmentChanger != null) {
            mFragmentChanger.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }
}