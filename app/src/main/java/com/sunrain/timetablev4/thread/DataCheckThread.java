package com.sunrain.timetablev4.thread;

import android.content.DialogInterface;

import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.activity.MainActivity;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.utils.WebUtil;

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
        final MainActivity mainActivity = mMainActivityWeakReference.get();

        if (mLastVersionCode < 23) {
            if (mainActivity == null) {
                ToastUtil.postShow("请在更多中查看使用教程", true);
                return;
            } else {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTutorialDialog(mainActivity);
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

        final int week = SharedPreUtils.getInt(SharedPreConstants.SEMESTER_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        int currentWeek = CalendarUtil.getCurrentWeek();
        if (currentWeek < 0 || currentWeek > week - 1) {
            ToastUtil.postShow("当前周数已超出学期总周数", true);
            return;
        }

        if (TableDao.existsOutOfWeek(week - 1)) {
            if (mainActivity == null) {
                ToastUtil.postShow("存在上课时间超出" + week + "周的课程", true);
                return;
            } else {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showOutOfWeekDialog(mainActivity, week);
                    }
                });
            }
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

    private void showTutorialDialog(final MainActivity mainActivity) {
        new MessageDialog(mainActivity).setMessage("建议您先查看使用教程，\n或稍后在更多中重新查看。")
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
                        WebUtil.gotoWeb(mainActivity, "http://timetable.gujin.tech/tutorial.html");
                    }
                })
                .show();
    }

    private void showOutOfWeekDialog(MainActivity mainActivity, int week) {
        new MessageDialog(mainActivity).setMessage("当前学期总周数为" + week + "周，存在上课时间超出" + week + "周的课程，请注意处理。")
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
