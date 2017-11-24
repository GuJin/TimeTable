package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;
import com.sunrain.timetablev4.bean.ClassBean;

import java.util.List;


public class InputCourseDialog extends BaseDialog<InputCourseDialog> {

    private TextView mTextView;

    public InputCourseDialog(Context context, List<ClassBean> list) {
        super(context);

        StringBuilder sb = new StringBuilder();
        sb.append("是否将当前课程清空，并导入新课程？\n\n新课程信息：");

        for (ClassBean classBean : list) {
            sb.append("\n\n");
            sb.append(ClassBean.Format.getFormatString(classBean));
        }

        mTextView.setText(sb.toString());
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input_dialog, parent, false);
        mTextView = view.findViewById(R.id.tv_content);
        return view;
    }
}
