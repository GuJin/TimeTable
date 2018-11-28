package com.sunrain.timetablev4.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.dialog.CalendarDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.table.TableData;
import com.sunrain.timetablev4.view.table.TableView;

import java.util.List;

import tech.gujin.toast.ToastUtil;


public class CourseFragment extends BaseFragment implements View.OnClickListener, TableView.OnBoxClickListener, TableData
        .OnTableDataChangedListener {

    private MessageDialog mClassDialog;
    private ImageButton mImgBtnRestore;
    private TextView mTvWeek;
    private long mJumpDate;
    private TableData mTableData;
    private OnJumpDateDialogPositiveClickListener mJumpDateDialogPositiveClickListener;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mTvWeek = view.findViewById(R.id.tv_week);
        mImgBtnRestore = view.findViewById(R.id.imgBtn_restore);

    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mTableData = TableData.getInstance();

        if (savedInstanceState != null) {
            long jumpDate = savedInstanceState.getLong("JumpDate", 0);
            if (jumpDate != 0) {
                mJumpDate = jumpDate;
                mTableData.setCurrentWeek(CalendarUtil.getCurrentWeek(mJumpDate));
                mImgBtnRestore.setVisibility(DateUtils.isToday(mJumpDate) ? View.INVISIBLE : View.VISIBLE);
            }
        }

        initTableView();
        setShowingWeek();
        setListener();
    }

    private void setShowingWeek() {
        mTvWeek.setText(String.valueOf(mTableData.getCurrentWeek() + 1));
    }

    private void initTableView() {
        TableView tableView = new TableView(mActivity);
        tableView.setOnBoxClickListener(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout
                .LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = DensityUtil.dip2Px(50);
        tableView.setLayoutParams(layoutParams);
        ((FrameLayout) getView().findViewById(R.id.fl_root)).addView(tableView);
    }

    private void setListener() {
        mImgBtnRestore.setOnClickListener(this);
        mTvWeek.setOnClickListener(this);
        mTableData.registerOnTableDataChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_week:
                showJumpDateDialog();
                break;
            case R.id.imgBtn_restore:
                restore();
                break;
        }
    }

    private void showJumpDateDialog() {
        if (mJumpDateDialogPositiveClickListener == null) {
            mJumpDateDialogPositiveClickListener = new OnJumpDateDialogPositiveClickListener();
        }

        CalendarDialog calendarDialog = new CalendarDialog(mActivity).setPositiveButton(mJumpDateDialogPositiveClickListener)
                .setDateRange(SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0), SharedPreUtils.getLong
                        (SharedPreConstants.SEMESTER_END_DATE, 0))
                .setDate(mJumpDate);
        mJumpDateDialogPositiveClickListener.setDialog(calendarDialog);
        calendarDialog.show();
    }

    @Override
    public void onContentChange() {
        setShowingWeek();
    }

    @Override
    public void onLayoutChange() {
    }

    private class OnJumpDateDialogPositiveClickListener implements DialogInterface.OnClickListener {

        private CalendarDialog mCalendarDialog;

        private void setDialog(CalendarDialog calendarDialog) {
            mCalendarDialog = calendarDialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            long jumpDate = mCalendarDialog.getDate();
            if (mJumpDate == jumpDate) {
                return;
            }

            mJumpDate = jumpDate;
            mTableData.setCurrentWeek(CalendarUtil.getCurrentWeek(mJumpDate));
            mTableData.refreshData();
            mImgBtnRestore.setVisibility(DateUtils.isToday(jumpDate) ? View.INVISIBLE : View.VISIBLE);
            setShowingWeek();
        }
    }

    private void restore() {
        mImgBtnRestore.setVisibility(View.GONE);
        mTableData.setCurrentWeek(CalendarUtil.getCurrentWeek());
        mTableData.refreshData();
        mJumpDate = 0;
        setShowingWeek();
    }

    @Override
    public void onBoxClick(int week, int section, int time) {
        showClassDialog(week, section, time);
    }

    private void showClassDialog(int week, int section, int time) {
        List<ClassBean> classes = TableDao.getClasses(week, section, time);
        if (classes.isEmpty()) {
            ToastUtil.show("无课程");
            return;
        }

        if (mClassDialog == null) {
            mClassDialog = new MessageDialog(mActivity).hideNegativeButton().setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            sb.append(ClassBean.Format.getFormatCourseClassroom(classes.get(i)));
            if (i < classes.size() - 1) {
                sb.append("\n\n");
            }
        }
        mClassDialog.setMessage(sb.toString());
        mClassDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mJumpDate != 0) {
            outState.putLong("JumpDate", mJumpDate);
        }
        super.onSaveInstanceState(outState);
    }
}
