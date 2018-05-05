package com.sunrain.timetablev4.manager.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.sunrain.timetablev4.R;


class ActivityPermissionManager extends BasePermissionManager {

    private final Activity mActivity;
    private final OnRequestPermissionsListener mListener;

    ActivityPermissionManager(Activity activity, OnRequestPermissionsListener listener) {
        mActivity = activity;
        mListener = listener;
    }

    @Override
    public void checkPermission(String[] permissions, int requestCode, int hintMessageId, int messageId) {
        String hintMessage = hintMessageId == 0 ? null : mActivity.getString(hintMessageId);
        String message = messageId == 0 ? null : mActivity.getString(messageId);
        checkPermission(permissions, requestCode, hintMessage, message);
    }

    @Override
    public void checkPermission(final String[] permissions, final int requestCode, String hintMessage, String message) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            mListener.onPermissionGranted(requestCode);
            return;
        }

        if (!TextUtils.isEmpty(message)) {
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                    new AlertDialog.Builder(mActivity, android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(message)
                            .setCancelable(false).setPositiveButton(R.string.i_know, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
                        }
                    }).show();
                    return;
                }
            }
        }

        if (!TextUtils.isEmpty(hintMessage)) {
            new AlertDialog.Builder(mActivity, android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(hintMessage)
                    .setCancelable(false).setPositiveButton(R.string.i_know, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
                }
            }).show();
            return;
        }

        ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isGranted(grantResults)) {
            mListener.onPermissionGranted(requestCode);
            return;
        }

        boolean neverAskAgainChecked = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED && !ActivityCompat
                    .shouldShowRequestPermissionRationale(mActivity, permissions[i])) {
                neverAskAgainChecked = true;
                break;
            }
        }
        mListener.onPermissionDenied(requestCode, neverAskAgainChecked);
    }
}