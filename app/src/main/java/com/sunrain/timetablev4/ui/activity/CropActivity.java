package com.sunrain.timetablev4.ui.activity;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseActivity;
import com.sunrain.timetablev4.view.CropImageView.CropImageView;
import com.sunrain.timetablev4.view.CropImageView.callback.CropCallback;
import com.sunrain.timetablev4.view.CropImageView.callback.LoadCallback;

import tech.gujin.toast.ToastUtil;

public class CropActivity extends BaseActivity implements View.OnClickListener {

    private CropImageView mCropImageView;
    private View mProgressBar;

    @Override
    protected int getContentView() {
        return R.layout.activity_crop;
    }

    @Override
    protected void initView() {
        mCropImageView = findViewById(R.id.cropImageView);
        mProgressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        initImage();
        setListener();
    }

    private void initImage() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        mCropImageView.setCustomRatio(point.x, point.y);
        mCropImageView.setOutputMaxHeight(point.y);
        mCropImageView.setOutputMaxWidth(point.x);
        mCropImageView.setDisplay(point.x, point.y);
        mCropImageView.startLoad((Uri) getIntent().getParcelableExtra("imageUrl"), new LoadCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                ToastUtil.show("加载图片失败");
                finish();
            }
        });
    }

    private void setListener() {
        findViewById(R.id.imgBtn_rotate_left).setOnClickListener(this);
        findViewById(R.id.imgBtn_rotate_right).setOnClickListener(this);
        findViewById(R.id.imgBtn_done).setOnClickListener(this);
        findViewById(R.id.imgBtn_close).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.imgBtn_rotate_left:
                mCropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                break;
            case R.id.imgBtn_rotate_right:
                mCropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.imgBtn_close:
                onBackPressed();
                break;
            case R.id.imgBtn_done:
                v.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                mCropImageView.startCrop(new CropCallback() {
                    @Override
                    public void onSuccess() {
                        CropActivity.this.setResult(RESULT_OK);
                        CropActivity.this.finish();
                    }

                    @Override
                    public void onError() {
                        ToastUtil.show("图片裁剪失败");
                        v.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
                break;

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
