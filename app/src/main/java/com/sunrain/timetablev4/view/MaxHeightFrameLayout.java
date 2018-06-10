package com.sunrain.timetablev4.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.sunrain.timetablev4.application.MyApplication;

public class MaxHeightFrameLayout extends FrameLayout {

    private static final int MAX_HEIGHT;

    static {
        DisplayMetrics displayMetrics = MyApplication.sContext.getResources().getDisplayMetrics();
        MAX_HEIGHT = (int) (displayMetrics.heightPixels * 0.8);
    }

    public MaxHeightFrameLayout(@NonNull Context context) {
        super(context);
    }

    public MaxHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getSize(heightMeasureSpec) > MAX_HEIGHT) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MAX_HEIGHT, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
