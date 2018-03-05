package com.sunrain.timetablev4.adapter.course_management;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseListAdapter;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.bean.ClassTimeBean;
import com.sunrain.timetablev4.bean.CourseClassroomBean;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.dialog.ClassTimeDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.view.table.TableData;


public class ClassTimeAdapter extends BaseListAdapter<ClassBean, ClassTimeAdapter.ViewHolder> {


    private final Context mContext;
    private final ListView mListView;
    private final int mSmoothOffset;

    private CourseClassroomBean mCourseClassroomBean;
    private ClassTimeDialog mClassTimeDialog;
    private boolean isDoubleWeekEnabled;

    public ClassTimeAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
        mSmoothOffset = DensityUtil.dip2Px(60);
    }

    @Override
    protected void onGetView(ViewHolder viewHolder, int position) {
        viewHolder.mtvTime.setText(ClassBean.Format.getFormatTime(mList.get(position)));
        viewHolder.mListener.setPosition(position);
    }

    @Override
    protected View createConvertView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.item_class_time, parent, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(View convertView) {
        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.mListener = new OnButtonClickListener();
        viewHolder.mImgBtnDelete.setOnClickListener(viewHolder.mListener);
        viewHolder.mImgBtnEdit.setOnClickListener(viewHolder.mListener);
        return viewHolder;
    }

    public void setCourseClassroom(@NonNull CourseClassroomBean courseClassroomBean) {
        mCourseClassroomBean = courseClassroomBean;
        mList.clear();
        mList.addAll(TableDao.getClassesList(mCourseClassroomBean));
        notifyDataSetChanged();
    }

    public void showAddDialog() {

        if (mClassTimeDialog == null) {
            initClassTimeDialog();
        }

        ClassBean classBean = new ClassBean();
        classBean._id = -1; // -1 代表新增
        classBean.course = mCourseClassroomBean.course;
        classBean.classroom = mCourseClassroomBean.classroom;
        mClassTimeDialog.setClassBean(classBean);

        mClassTimeDialog.show();
    }

    private void showEditDialog(ClassBean classBean) {
        if (mClassTimeDialog == null) {
            initClassTimeDialog();
        }

        mClassTimeDialog.setClassBean(classBean);
        mClassTimeDialog.show();
    }

    private void showDeleteHintDialog(final ClassBean classBean) {
        new MessageDialog(mContext).setMessage(R.string.dialog_delete).setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                TableDao.deleteInBackground(classBean);
                mList.remove(classBean);
                notifyDataSetChanged();
            }
        }).show();
    }

    private void initClassTimeDialog() {
        mClassTimeDialog = new ClassTimeDialog(mContext, isDoubleWeekEnabled);
        mClassTimeDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                ClassBean classBean = mClassTimeDialog.getClassBean();
                if (classBean._id == -1) {// 新增
                    classBean._id = TableDao.insert(classBean);
                    mList.add(classBean);
                } else {
                    TableDao.updateClassTimeInBackground(classBean._id, new ClassTimeBean(classBean));
                    for (int i = 0; i < mList.size(); i++) {
                        ClassBean bean = mList.get(i);
                        if (bean._id == classBean._id) {
                            mList.remove(i);
                            mList.add(i, classBean);
                        }
                    }
                }
                TableData.getInstance().setContentChange();
                notifyDataSetChanged();
                mListView.smoothScrollByOffset(mSmoothOffset);
            }
        });
    }

    public void setDoubleWeekEnabled(boolean doubleWeekEnabled) {
        isDoubleWeekEnabled = doubleWeekEnabled;
    }

    public void setDialogNull() {
        if (mClassTimeDialog != null && mClassTimeDialog.isShowing()) {
            mClassTimeDialog.dismiss();
        }
        mClassTimeDialog = null;
    }

    private class OnButtonClickListener implements View.OnClickListener {

        private int mPosition;

        private void setPosition(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            ClassBean classBean = mList.get(mPosition);
            switch (v.getId()) {
                case R.id.imgBtn_delete:
                    showDeleteHintDialog(classBean);
                    break;
                case R.id.imgBtn_edit:
                    showEditDialog(classBean);
                    break;
            }
        }
    }

    protected static final class ViewHolder extends BaseListAdapter.ViewHolder {

        TextView mtvTime;
        ImageButton mImgBtnDelete;
        ImageButton mImgBtnEdit;
        OnButtonClickListener mListener;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mtvTime = itemView.findViewById(R.id.tv_time);
            mImgBtnDelete = itemView.findViewById(R.id.imgBtn_delete);
            mImgBtnEdit = itemView.findViewById(R.id.imgBtn_edit);
        }
    }
}
