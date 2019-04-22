package com.sunrain.timetablev4.view;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;


public class UserSpinner extends Spinner {

    private OnItemSelectedByUserListener mOnItemSelectedByUserListener;
    private boolean isUserOpen;
    private OnItemSelectedListener mOnItemSelectedListener;

    public UserSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        registerEvents();
    }

    private void registerEvents() {
        super.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(parent, view, position, id);
                }
                if (isUserOpen && mOnItemSelectedByUserListener != null) {
                    mOnItemSelectedByUserListener.onItemSelectByUser(parent, view, position, id);
                    isUserOpen = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onNothingSelected(parent);
                }
            }
        });
    }

    public void setOnItemSelectedByUserListener(OnItemSelectedByUserListener onItemSelectedByUserListener) {
        mOnItemSelectedByUserListener = onItemSelectedByUserListener;
    }

    @Override
    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    @Override
    public boolean performClick() {
        isUserOpen = true;
        return super.performClick();
    }

    public interface OnItemSelectedByUserListener {

        void onItemSelectByUser(AdapterView<?> parent, View view, int position, long id);
    }
}
