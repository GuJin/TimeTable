package com.sunrain.timetablev4.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.sunrain.timetablev4.R;

public class AppWidgetConfigureActivity extends Activity implements View.OnClickListener {

    private int mAppWidgetId;
    private RadioGroup mRgBgColor;
    private SeekBar mSbTransparent;
    private Button mBtnConfirm;
    private Button mBtnCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.activity_appwidget_configure);

        initView();
        setListener();
    }


    private void initView() {
        mRgBgColor = findViewById(R.id.rg_bg_color);
        mSbTransparent = findViewById(R.id.sb_transparent);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);
    }

    private void setListener() {
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                DayAppWidgetProvider.updateAppWidget(appWidgetManager, mAppWidgetId, 0x33000000);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
