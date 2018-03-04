package com.sunrain.timetablev4.adapter.course_management;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseListAdapter;
import com.sunrain.timetablev4.bean.CourseClassroomBean;

import java.util.List;


public class CourseClassroomAdapter extends BaseListAdapter<CourseClassroomBean, CourseClassroomAdapter.ViewHolder> {

    private final StringBuilder mSb;
    private int mClickPosition = -1;
    private int mClickColor;

    public CourseClassroomAdapter(List<CourseClassroomBean> courseClassroomList) {
        super(courseClassroomList);
        mSb = new StringBuilder();
        mClickColor = 0x20ffffff;
    }

    @Override
    protected View createConvertView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.item_course_classroom, parent, false);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    protected void onGetView(ViewHolder viewHolder, int position) {
        CourseClassroomBean bean = mList.get(position);
        mSb.setLength(0);
        mSb.append(bean.course).append("\n").append(bean.classroom);
        viewHolder.mTvCourseClassroom.setText(mSb.toString());
        if (position == mClickPosition) {
            viewHolder.mTvCourseClassroom.setBackgroundColor(mClickColor);
        } else {
            viewHolder.mTvCourseClassroom.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setClickPosition(int clickPosition) {
        mClickPosition = clickPosition;
    }

    public int getClickPosition() {
        return mClickPosition;
    }

    public static final class ViewHolder extends BaseListAdapter.ViewHolder {

        private TextView mTvCourseClassroom;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mTvCourseClassroom = itemView.findViewById(R.id.tv_course_classroom);
        }
    }
}
