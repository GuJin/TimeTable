package com.sunrain.timetablev4.ui.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;

public class MessageDialog extends BaseDialog<MessageDialog> {

    private TextView mTextView;

    public MessageDialog(Context context) {
        super(context);
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, parent, false);
        mTextView = view.findViewById(R.id.tv_message);
        return view;
    }

    public MessageDialog setMessage(@NonNull String msg) {
        mTextView.setText(msg);
        return this;
    }

    public MessageDialog setMessage(@StringRes int msg) {
        mTextView.setText(msg);
        return this;
    }

}
