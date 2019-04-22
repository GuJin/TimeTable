package com.sunrain.timetablev4.base;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.utils.SystemUiUtil;

@SuppressWarnings("unchecked")
public class BaseDialog<T extends BaseDialog> extends Dialog {

    private FrameLayout mFlContent;
    private Button mBtnDialogNegative;
    private Button mBtnDialogPositive;
    private View mViewSplit;

    private OnClickListener mNegativeListener;
    private OnClickListener mPositiveListener;
    private int mRequireWidth;

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
        View view = initView(context);
        View contentView = getContentView(context, mFlContent);
        mFlContent.addView(contentView);
        setListener();
        setContentView(view);
    }

    protected View getContentView(Context context, ViewGroup parent) {
        return null;
    }

    @NonNull
    private View initView(Context context) {
        View view = View.inflate(context, R.layout.dialog_base, null);
        mFlContent = view.findViewById(R.id.fl_content);
        mBtnDialogNegative = view.findViewById(R.id.btn_dialog_negative);
        mViewSplit = view.findViewById(R.id.view_split);
        mBtnDialogPositive = view.findViewById(R.id.btn_dialog_positive);
        return view;
    }

    protected void setRequireWidth(int requireWidth) {
        mRequireWidth = requireWidth;
    }

    private void setListener() {
        OnButtonClickListener listener = new OnButtonClickListener();
        mBtnDialogNegative.setOnClickListener(listener);
        mBtnDialogPositive.setOnClickListener(listener);
    }

    public void setContent(View view) {
        mFlContent.addView(view);
    }

    public T setPositiveButton(DialogInterface.OnClickListener positiveListener) {
        return setPositiveButton(null, positiveListener);
    }

    public T setPositiveButton(String text, DialogInterface.OnClickListener positiveListener) {
        mPositiveListener = positiveListener;
        if (!TextUtils.isEmpty(text)) {
            mBtnDialogPositive.setText(text);
        }
        return (T) this;
    }

    public T setNegativeButton(DialogInterface.OnClickListener negativeListener) {
        return setNegativeButton(null, negativeListener);
    }

    public T setNegativeButton(@Nullable String text, DialogInterface.OnClickListener negativeListener) {
        mNegativeListener = negativeListener;
        if (!TextUtils.isEmpty(text)) {
            mBtnDialogNegative.setText(text);
        }
        return (T) this;
    }

    private class OnButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_dialog_negative && mNegativeListener != null) {
                mNegativeListener.onClick(BaseDialog.this, DialogInterface.BUTTON_NEGATIVE);
            } else if (id == R.id.btn_dialog_positive && mPositiveListener != null) {
                mPositiveListener.onClick(BaseDialog.this, DialogInterface.BUTTON_NEGATIVE);
            }
        }
    }

    public T hideNegativeButton() {
        mBtnDialogNegative.setVisibility(View.GONE);
        mViewSplit.setVisibility(View.GONE);
        mBtnDialogPositive.setBackgroundResource(R.drawable.dialog_btn_only_positive);
        return (T) this;
    }

    public T hideButton() {
        findViewById(R.id.ll_button).setVisibility(View.GONE);
        findViewById(R.id.view).setVisibility(View.GONE);
        return (T) this;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || mRequireWidth == 0) {
            return;
        }

        Window window = getWindow();
        if (window == null) {
            return;
        }

        int width = window.getDecorView().getWidth();

        if (width == mRequireWidth) {
            return;
        }

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = mRequireWidth;

        window.setAttributes(attributes);
    }

    @Override
    public void show() {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        // 防止 NavigationBar 弹出
        // 参考：https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs/23207365
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        SystemUiUtil.setSystemUi(window.getDecorView());
        super.show();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void superShow() {
        super.show();
    }
}
