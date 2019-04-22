/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import com.google.zxing.*;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.manager.permission.PermissionManager;
import com.sunrain.timetablev4.utils.RunnableExecutorService;
import com.sunrain.timetablev4.view.CropImageView.util.Utils;
import tech.gujin.toast.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = "CaptureActivity";
    private final int PERMISSION_PICK_IMAGE = 10;
    private final int REQUEST_PICK_IMAGE = 12;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private PermissionManager mPermissionManager;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int navigationHeight = getNavigationHeight();
        if (navigationHeight > 0) {
            window.setNavigationBarColor(0x00000000);
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.activity_capture);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        findViewById(R.id.btn_photo).setOnClickListener(this);
    }

    private int getNavigationHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(this);

        viewfinderView = findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        handler = null;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        inactivityTimer.onResume();

        Intent intent = getIntent();

        decodeFormats = null;
        characterSet = null;

        if (intent != null) {
            characterSet = intent.getStringExtra("CHARACTER_SET");
        }

        SurfaceView surfaceView = findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }


    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void decodeOrStoreSavedBitmap() {
        if (handler == null) {
            savedResultToShow = null;
        } else {
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void handleDecode(String resultString) {
        inactivityTimer.onActivity();
        if (TextUtils.isEmpty(resultString)) {
            ToastUtil.show("二维码错误");
            finish();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("result", resultString);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, null, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.confirm, new FinishListener(this));
        builder.show();
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_photo:
                checkPhotoPermission();
                break;
        }
    }

    private void checkPhotoPermission() {
        if (mPermissionManager == null) {
            mPermissionManager = PermissionManager.Factory.get(this, new PermissionManager.OnRequestPermissionsListener() {
                @Override
                public void onPermissionGranted(int requestCode) {
                    if (requestCode == PERMISSION_PICK_IMAGE) {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                                REQUEST_PICK_IMAGE);
                    }
                }

                @Override
                public void onPermissionDenied(int requestCode, boolean neverAskAgainChecked) {
                    if (requestCode == PERMISSION_PICK_IMAGE) {
                        ToastUtil.show(getString(R.string.permission_read_fail_background), true);
                    }
                }
            });
        }
        mPermissionManager.checkPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_PICK_IMAGE, 0, R.string
                .permission_read_message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                analysisImage(data.getData());
                break;
        }

    }

    private void analysisImage(final Uri uri) {
        if (uri == null) {
            ToastUtil.show("加载图片失败");
            return;
        }
        //解析图片
        RunnableExecutorService.when(new Callable<String>() {
            @Override
            public String call() {
                Bitmap sampledBitmap = Utils.decodeSampledBitmapFromUri(MyApplication.sContext, uri, 600);
                return syncDecodeQRCode(sampledBitmap, false);
            }
        }).done(new RunnableExecutorService.DoneCallback<String>() {
            @Override
            public void done(String result) {
                handleDecode(result);
            }
        }).execute();
    }

    public String syncDecodeQRCode(Bitmap bitmap, boolean retry) {
        HashMap<DecodeHintType, Object> hintTypeObjectHashMap = new HashMap<>();
        List<BarcodeFormat> allFormats = new ArrayList<>(1);
        allFormats.add(BarcodeFormat.QR_CODE);
        hintTypeObjectHashMap.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        hintTypeObjectHashMap.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        if (retry) {
            hintTypeObjectHashMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hintTypeObjectHashMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        }

        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            Result result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), hintTypeObjectHashMap);
            if (result != null) {
                return result.getText();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof NotFoundException && !retry) {
                return syncDecodeQRCode(bitmap, true);
            } else {
                return null;
            }
        }
    }

//    private String syncDecodeQRCodeByYUV(Bitmap bitmap) {
//        final byte[] bitmapYUVBytes = ImageUtil.getBitmapYUVBytes(bitmap);
//        if (bitmapYUVBytes == null) {
//            return null;
//        }
//        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(bitmapYUVBytes, bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap
//                .getWidth(), bitmap.getHeight(), true);
//        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
//        Reader reader = new QRCodeReader();
//        Result result;
//        try {
//            result = reader.decode(binaryBitmap);
//        } catch (NotFoundException | ChecksumException | FormatException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return result.getText();
//    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull final int[] grantResults) {
        mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
