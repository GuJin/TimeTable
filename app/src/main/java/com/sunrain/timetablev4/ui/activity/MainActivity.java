package com.sunrain.timetablev4.ui.activity;

import android.content.DialogInterface;
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
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.ui.fragment.CourseFragment;
import com.sunrain.timetablev4.ui.fragment.SettingsFragment;
import com.sunrain.timetablev4.utils.ChannelHelper;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.utils.WebUtil;
import com.sunrain.timetablev4.view.DrawerArrowDrawable;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String KEY_SAVE_FRAGMENT = "fragment";
    private FragmentChanger mFragmentChanger;

    private ImageButton mImgBtnSettings;
    private DrawerArrowDrawable mArrow;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String lastFragmentName = mFragmentChanger == null ? null : mFragmentChanger.getLastFragmentName();
        if (!TextUtils.isEmpty(lastFragmentName)) {
            outState.putString(KEY_SAVE_FRAGMENT, lastFragmentName);
        }
    }

    @Override
    protected void initView() {
        mImgBtnSettings = findViewById(R.id.imgBtn_settings);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        initBugly();
        initFragment(savedInstanceState);
        initPost();
    }

    private void initPost() {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                initArrow();
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

    private void initFragment(Bundle savedInstanceState) {
        mFragmentChanger = new FragmentChanger(getFragmentManager(), R.id.fl_main);
        String fragment = savedInstanceState == null ? null : savedInstanceState.getString(KEY_SAVE_FRAGMENT);
        if (TextUtils.isEmpty(fragment) || CourseFragment.class.getSimpleName().equals(fragment)) {
            mFragmentChanger.showFragment(CourseFragment.class);
            return;
        }
        //can not use switch there,has constant expression required error
        if (SettingsFragment.class.getSimpleName().equals(fragment)) {
            mFragmentChanger.showFragment(SettingsFragment.class);
        }
    }

    private void setListener() {
        mImgBtnSettings.setOnClickListener(this);
    }

    private void initArrow() {
        mArrow = new DrawerArrowDrawable();
        mArrow.setAnimationListener(new DrawerArrowDrawable.AnimationListener() {
            @Override
            public void onAnimationFinish() {
                mImgBtnSettings.setEnabled(true);
            }
        });
        mImgBtnSettings.setImageDrawable(mArrow);
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

    public void showTutorialDialog() {
        new MessageDialog(mContext).setMessage("建议您先查看使用教程，或稍后在更多中重新查看。")
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("查看使用教程", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WebUtil.gotoWeb(mContext, "http://timetable.gujin.tech/tutorial.html");
                    }
                })
                .show();
    }
}