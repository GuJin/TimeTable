package com.sunrain.timetablev4.thread;

import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.activity.MainActivity;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;

import java.lang.ref.WeakReference;

import tech.gujin.toast.ToastUtil;

public class DataCheckThread extends Thread {

    private final int mLastVersionCode;
    private final WeakReference<MainActivity> mMainActivityWeakReference;

    public DataCheckThread(MainActivity mainActivity, int lastVersionCode) {
        mMainActivityWeakReference = new WeakReference<>(mainActivity);
        mLastVersionCode = lastVersionCode;
    }

    @Override
    public void run() {
        if (mLastVersionCode < 23) {
            final MainActivity mainActivity = mMainActivityWeakReference.get();
            if (mainActivity == null) {
                ToastUtil.postShow("请在更多中查看使用教程", true);
                return;
            } else {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.showTutorialDialog();
                    }
                });
            }
            return;
        }

        // 学期检查
        long startDateTime = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        if (startDateTime == 0) {
            ToastUtil.postShow("请设置学期起始日期", true);
            return;
        }

        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);
        if (endDate == 0) {
            ToastUtil.postShow("请设置学期结束日期", true);
            return;
        }

        int week = SharedPreUtils.getInt(SharedPreConstants.SEMESTER_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        int currentWeek = CalendarUtil.getCurrentWeek();
        if (currentWeek < 0 || currentWeek > week) {
            ToastUtil.postShow("当前日期已超出学期时间", true);
            return;
        }

        // 课程检查
        if (CourseClassroomDao.isDataBaseEmpty()) {
            ToastUtil.postShow("请添加课程", true);
            return;
        }

        if (TableDao.isDataBaseEmpty()) {
            ToastUtil.postShow("请为课程添加上课时间", true);
            return;
        }

        if (SharedPreUtils.getInt(SharedPreConstants.DOUBLE_WEEK, SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 0 && TableDao
                .existsDoubleWeek()) {
            ToastUtil.postShow("存在双周课程，但未启用单双周功能", true);
        }
    }
}
