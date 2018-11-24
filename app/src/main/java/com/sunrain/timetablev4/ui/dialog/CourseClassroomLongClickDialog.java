package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;
import com.sunrain.timetablev4.bean.CourseClassroomBean;


public class CourseClassroomLongClickDialog extends BaseDialog<CourseClassroomLongClickDialog> implements View.OnClickListener {

    private final OnClickListener mOnClickListener;
    private View mView;

    public CourseClassroomLongClickDialog(Context context, CourseClassroomBean bean, DialogInterface.OnClickListener onClickListener) {
        super(context);
        mOnClickListener = onClickListener;

        ((TextView)mView.findViewById(R.id.tv_title)).setText(String.format("%s %s", bean.course, bean.classroom));
        mView.findViewById(R.id.tv_edit).setOnClickListener(this);
        mView.findViewById(R.id.tv_delete).setOnClickListener(this);

        hideButton();
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_course_classroom_long_click, parent, false);
        return mView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_edit:
                mOnClickListener.onClick(this, 0);
                break;
            case R.id.tv_delete:
                mOnClickListener.onClick(this, 1);
                break;
        }
    }
}
