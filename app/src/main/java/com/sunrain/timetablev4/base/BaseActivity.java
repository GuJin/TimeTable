package com.sunrain.timetablev4.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.sunrain.timetablev4.utils.SystemUiUtil;

public abstract class BaseActivity extends Activity {

    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        mContext = this;
        initView();
        initData(savedInstanceState);
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void initData(Bundle savedInstanceState);

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            SystemUiUtil.setSystemUi(getWindow().getDecorView());
        }
    }
}
