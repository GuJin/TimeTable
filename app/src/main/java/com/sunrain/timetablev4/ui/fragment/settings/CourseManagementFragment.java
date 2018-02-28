package com.sunrain.timetablev4.ui.fragment.settings;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.adapter.course_management.ClassTimeAdapter;
import com.sunrain.timetablev4.adapter.course_management.CourseClassroomAdapter;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.bean.CourseClassroomBean;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.SystemUiUtil;
import com.sunrain.timetablev4.view.table.TableData;

import java.util.List;

import tech.gujin.toast.ToastUtil;

public class CourseManagementFragment extends BaseFragment implements ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private View mRootView;
    private ListView mLvCourseClassroom;
    private ListView mLvClassTime;
    private EditText mEtCourse;
    private EditText mEtClassroom;
    private List<CourseClassroomBean> mCourseClassroomList;
    private CourseClassroomAdapter mCourseClassroomAdapter;
    private ClassTimeAdapter mClassTimeAdapter;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_course_management, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mRootView = view.findViewById(R.id.cl_root);
        mLvCourseClassroom = view.findViewById(R.id.lv_course_classroom);
        mLvClassTime = view.findViewById(R.id.lv_class_time);
    }

    @Override
    public void initData() {
        initCourseClassroomListView();
        initClassTimeListView();
        setListener();
    }

    private void initCourseClassroomListView() {
        mCourseClassroomList = CourseClassroomDao.getAll();
        mCourseClassroomAdapter = new CourseClassroomAdapter(mCourseClassroomList);
        mLvCourseClassroom.setAdapter(mCourseClassroomAdapter);

        View view = View.inflate(mActivity, R.layout.footer_course_classroom_listview, null);
        mEtCourse = view.findViewById(R.id.et_course);
        mEtClassroom = view.findViewById(R.id.et_classroom);
        view.findViewById(R.id.btn_add_course_classroom).setOnClickListener(this);
        mLvCourseClassroom.addFooterView(view, null, false);
    }

    private void initClassTimeListView() {
        mClassTimeAdapter = new ClassTimeAdapter(mActivity, mLvClassTime);
        mLvClassTime.setAdapter(mClassTimeAdapter);

        View view = View.inflate(mActivity, R.layout.footer_class_time_listview, null);
        view.findViewById(R.id.btn_add_class_time).setOnClickListener(this);
        mLvClassTime.addFooterView(view, null, false);
    }

    private void setListener() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mLvCourseClassroom.setOnItemClickListener(this);
        mLvCourseClassroom.setOnItemLongClickListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // 软键盘隐藏时 重新设置全屏模式
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = mRootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        if (keypadHeight < screenHeight * 0.15) {
            SystemUiUtil.setSystemUi(getActivity().getWindow().getDecorView());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_course_classroom:
                checkValid();
                break;
            case R.id.btn_add_class_time:
                mClassTimeAdapter.showAddDialog();
                break;
        }
    }

    private void checkValid() {
        String course = mEtCourse.getText().toString();
        String classroom = mEtClassroom.getText().toString();

        if (TextUtils.isEmpty(course)) {
            ToastUtil.show("课程名称不能为空");
            return;
        }

        if (TextUtils.isEmpty(classroom)) {
            ToastUtil.show("上课地点不能为空");
            return;
        }

        CourseClassroomBean bean = new CourseClassroomBean(course, classroom);
        if (CourseClassroomDao.exists(bean)) {
            ToastUtil.show("已有相同条目");
            return;
        }

        save(bean);
    }

    private void save(CourseClassroomBean bean) {
        CourseClassroomDao.insertInBackground(bean);
        mCourseClassroomList.add(bean);
        mCourseClassroomAdapter.notifyDataSetChanged();
        mLvCourseClassroom.smoothScrollByOffset(200);

        mEtCourse.setText("");
        mEtClassroom.setText("");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CourseClassroomBean classroomBean = mCourseClassroomAdapter.getItem(position);
        mClassTimeAdapter.setCourseClassroom(classroomBean);
        mCourseClassroomAdapter.setClickPosition(position);
        mCourseClassroomAdapter.notifyDataSetChanged();
        if (mLvClassTime.getVisibility() == View.INVISIBLE) {
            mLvClassTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv_course_classroom:
                showDeleteCourseClassroomDialog(mCourseClassroomAdapter.getItem(position));
                return true;
        }
        return false;
    }

    private void showDeleteCourseClassroomDialog(final CourseClassroomBean bean) {
        new MessageDialog(mActivity).setMessage("删除 " + bean.course + " " + bean.classroom + " 下所有课程？")
                .setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CourseClassroomDao.deleteInBackground(bean);
                        TableDao.deleteInBackground(bean);
                        mCourseClassroomList.remove(bean);
                        mCourseClassroomAdapter.notifyDataSetChanged();
                        TableData.getInstance().setContentChange();
                    }
                })
                .show();
    }
}
