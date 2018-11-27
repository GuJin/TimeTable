package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;
import com.sunrain.timetablev4.bean.CourseClassroomBean;


public class CourseClassroomEditDialog extends BaseDialog<CourseClassroomEditDialog> {

    private EditText mEtCourse;
    private EditText mEtClassroom;

    public CourseClassroomEditDialog(Context context, CourseClassroomBean bean) {
        super(context);
        mEtCourse.setText(bean.course);
        mEtClassroom.setText(bean.classroom);
        mEtCourse.setSelection(bean.course.length());
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_course_classroom_edit, parent, false);
        mEtCourse = view.findViewById(R.id.et_course);
        mEtClassroom = view.findViewById(R.id.et_classroom);
        return view;
    }

    public String getCourse() {
        return mEtCourse.getText().toString().trim();
    }

    public String getClassroom() {
        return mEtClassroom.getText().toString().trim();
    }

    @Override
    public void show() {
        Window window = getWindow();
        if (window != null) {
            // dialog 内 editText 不会自动弹出输入框
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        superShow();
    }
}
