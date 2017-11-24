package com.sunrain.timetablev4.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.sunrain.timetablev4.R;

public class PermissionManager {

    private final OnRequestPermissionsListener mListener;

    public PermissionManager(OnRequestPermissionsListener listener) {
        mListener = listener;
    }

    public void checkPermission(final Fragment fragment, final String[] permissions, final int requestCode, int messageId) {

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }


        if (allGranted) {
            mListener.onGranted(requestCode);
            return;
        }

        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(fragment, permission)) {
                new AlertDialog.Builder(fragment.getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(fragment
                        .getString(messageId))
                        .setPositiveButton(fragment.getString(R.string.i_know), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FragmentCompat.requestPermissions(fragment, permissions, requestCode);
                            }
                        })
                        .show();
                return;
            }
        }

        FragmentCompat.requestPermissions(fragment, permissions, requestCode);

    }

    public void checkPermission(final Activity activity, final String[] permissions, final int requestCode, int messageId) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }


        if (allGranted) {
            mListener.onGranted(requestCode);
            return;
        }

        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert).setMessage(activity.getString
                        (messageId))
                        .setPositiveButton(activity.getString(R.string.i_know), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                            }
                        })
                        .show();
                return;
            }
        }

        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (isGranted(grantResults)) {
            mListener.onGranted(requestCode);
        } else {
            mListener.onDenied(requestCode);
        }
    }


    private boolean isGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public interface OnRequestPermissionsListener {

        void onGranted(int requestCode);

        void onDenied(int requestCode);
    }
}
