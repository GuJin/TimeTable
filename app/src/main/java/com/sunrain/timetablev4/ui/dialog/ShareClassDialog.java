package com.sunrain.timetablev4.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.utils.ClassQrCodeHelper;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.ImageUtil;
import com.sunrain.timetablev4.utils.ZipUtil;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.gujin.toast.ToastUtil;


public class ShareClassDialog extends Dialog implements View.OnClickListener {

    private final AsyncTask<Void, Integer, Bitmap> mAsyncTask;

    private TextView mTvProgress;
    private ImageView mIvQrCode;
    private ImageButton mImgBtnSave;
    private ImageButton mImgBtnClose;

    public ShareClassDialog(Context context) {
        super(context, R.style.MyDialog);
        View view = View.inflate(context, R.layout.dialog_share_class, null);
        mTvProgress = view.findViewById(R.id.tv_progress);
        mIvQrCode = view.findViewById(R.id.iv_qr_code);
        mImgBtnSave = view.findViewById(R.id.imgBtn_save);
        mImgBtnClose = view.findViewById(R.id.imgBtn_close);

        // 设置二维码面积
        ViewGroup.LayoutParams layoutParams = mIvQrCode.getLayoutParams();
        float density = MyApplication.sContext.getResources().getDisplayMetrics().heightPixels;
        int square = (int) (density - DensityUtil.dip2Px(50));
        layoutParams.width = square;
        layoutParams.height = square;
        mIvQrCode.setLayoutParams(layoutParams);
        setContentView(view);

        mAsyncTask = new CreateQRCodeAsync(this, square).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtn_close:
                dismiss();
                break;
            case R.id.imgBtn_save:
                BitmapDrawable drawable = (BitmapDrawable) mIvQrCode.getDrawable();
                ImageUtil.saveBitmapToPicturesDirectory("课表.jpg", drawable.getBitmap());
                break;
        }
    }

    private static class CreateQRCodeAsync extends AsyncTask<Void, Integer, Bitmap> {

        private final int mSquare;
        private StringBuilder mProgressSB;

        private WeakReference<ShareClassDialog> mShareClassDialogWeakReference;

        CreateQRCodeAsync(ShareClassDialog shareClassDialog, int square) {
            mShareClassDialogWeakReference = new WeakReference<>(shareClassDialog);
            mSquare = square;
            mProgressSB = new StringBuilder();
        }

        protected Bitmap doInBackground(Void... params) {
            publishProgress(1);

            List<ClassBean> classes = TableDao.getClasses();
            publishProgress(8);

            String json;
            try {
                json = ClassQrCodeHelper.toJSONObject(classes).toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            publishProgress(12);

            String zip = ZipUtil.zipString(json);
            publishProgress(19);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);//二维码容错率
            hints.put(EncodeHintType.MARGIN, 0);//二维码边框
            BitMatrix bitMatrix;
            try {
                bitMatrix = new QRCodeWriter().encode(zip, BarcodeFormat.QR_CODE, mSquare, mSquare, hints);
                publishProgress(24);
            } catch (WriterException e) {
                e.printStackTrace();
                return null;
            }

            int height = bitMatrix.getHeight();
            int width = bitMatrix.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            int currentProgress = 30;

            for (int x = 0; x < width; x++) {

                if (isCancelled()) {
                    return null;
                }

                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }

                int progress = 70 * x / width + 30;
                if (progress != currentProgress) {
                    publishProgress(progress);
                    currentProgress = progress;
                }

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ShareClassDialog shareClassDialog = mShareClassDialogWeakReference.get();
            if (shareClassDialog == null) {
                ToastUtil.show("生成二维码错误");
                return;
            }

            if (bitmap != null) {
                shareClassDialog.mTvProgress.setVisibility(View.GONE);
                shareClassDialog.mIvQrCode.setImageBitmap(bitmap);
                shareClassDialog.mImgBtnSave.setVisibility(View.VISIBLE);
                shareClassDialog.mImgBtnClose.setVisibility(View.VISIBLE);
                shareClassDialog.mImgBtnClose.setOnClickListener(shareClassDialog);
                shareClassDialog.mImgBtnSave.setOnClickListener(shareClassDialog);
            } else {
                shareClassDialog.mTvProgress.setText("生成二维码错误");
                shareClassDialog.mImgBtnClose.setVisibility(View.VISIBLE);
                shareClassDialog.mImgBtnClose.setOnClickListener(shareClassDialog);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ShareClassDialog shareClassDialog = mShareClassDialogWeakReference.get();
            if (shareClassDialog == null) {
                return;
            }

            shareClassDialog.mTvProgress.setText(mProgressSB.append(values[0]).append("%"));
            mProgressSB.setLength(0);
        }

    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (mAsyncTask != null && !mAsyncTask.isCancelled()) {
            mAsyncTask.cancel(true);
        }

        Drawable drawable = mIvQrCode.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}
