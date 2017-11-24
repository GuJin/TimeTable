package com.sunrain.timetablev4.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ScrollView;

public class MaxHeightScrollView extends ScrollView {

    private final DisplayMetrics d;

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        d = new DisplayMetrics();
        display.getMetrics(d);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置控件高度不能超过屏幕高度三分之二
        int maxHeight = d.heightPixels / 3 * 2;
        if (MeasureSpec.getSize(heightMeasureSpec) > maxHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.getMode(heightMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
