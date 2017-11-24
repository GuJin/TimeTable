package com.sunrain.timetablev4.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.base.BaseDialog;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.ImageUtil;


public class DonationDialog extends BaseDialog implements View.OnLongClickListener, View.OnClickListener {

    private final Activity mActivity;
    private ImageView mImgBtnAli;
    private ImageView mImgBtnWechat;

    public DonationDialog(Activity activity) {
        super(activity);
        mActivity = activity;
        hideNegativeButton();
        setPositiveButton("关闭", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        setRequireWidth(DensityUtil.dip2Px(330));
    }

    @Override
    protected View getContentView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_donation, parent, false);
        mImgBtnAli = view.findViewById(R.id.imgBtn_ali);
        mImgBtnWechat = view.findViewById(R.id.imgBtn_wechat);
        mImgBtnAli.setOnLongClickListener(this);
        mImgBtnWechat.setOnLongClickListener(this);
        mImgBtnAli.setOnClickListener(this);
        return view;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_ali:
                saveQRCode("支付宝.jpg", ((BitmapDrawable) mImgBtnAli.getDrawable()).getBitmap());
                break;
            case R.id.imgBtn_wechat:
                saveQRCode("微信.jpg", ((BitmapDrawable) mImgBtnWechat.getDrawable()).getBitmap());
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_ali:
                String url = "https://qr.alipay.com/aex08333cyvyipqgisnnk0e";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mActivity.startActivity(intent);
                break;
        }
    }

    private void saveQRCode(String name, Bitmap bitmap) {
        ImageUtil.saveBitmapToPicturesDirectory(name, bitmap);
    }
}
