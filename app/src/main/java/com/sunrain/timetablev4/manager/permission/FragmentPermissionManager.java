package com.sunrain.timetablev4.manager.permission;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.sunrain.timetablev4.R;


class FragmentPermissionManager extends BasePermissionManager {

    private final Fragment mFragment;
    private final Context mContext;
    private final OnRequestPermissionsListener mListener;

    FragmentPermissionManager(Fragment fragment, OnRequestPermissionsListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContext = fragment.getContext();
        } else {
            mContext = fragment.getActivity();
        }
        if (mContext == null) {
            throw new IllegalArgumentException("You cannot checkPermission on a null Context");
        }
        mFragment = fragment;
        mListener = listener;
    }

    @Override
    public void checkPermission(String[] permissions, int requestCode, int hintMessageId, int messageId) {
        String hintMessage = hintMessageId == 0 ? null : mFragment.getString(hintMessageId);
        String message = messageId == 0 ? null : mFragment.getString(messageId);
        checkPermission(permissions, requestCode, hintMessage, message);
    }

    @Override
    public void checkPermission(final String[] permissions, final int requestCode, String hintMessage, String message) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
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
                if (FragmentCompat.shouldShowRequestPermissionRationale(mFragment, permission)) {
                    new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(message)
                            .setCancelable(false).setPositiveButton(R.string.i_know, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(mFragment, permissions, requestCode);
                        }
                    }).show();
                    return;
                }
            }
        }

        if (!TextUtils.isEmpty(hintMessage)) {
            new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(hintMessage)
                    .setCancelable(false).setPositiveButton(mFragment.getString(R.string.i_know), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FragmentCompat.requestPermissions(mFragment, permissions, requestCode);
                }
            }).show();
            return;
        }

        FragmentCompat.requestPermissions(mFragment, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isGranted(grantResults)) {
            mListener.onPermissionGranted(requestCode);
            return;
        }

        boolean neverAskAgainChecked = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED && !FragmentCompat
                    .shouldShowRequestPermissionRationale(mFragment, permissions[i])) {
                neverAskAgainChecked = true;
                break;
            }
        }
        mListener.onPermissionDenied(requestCode, neverAskAgainChecked);
    }
}