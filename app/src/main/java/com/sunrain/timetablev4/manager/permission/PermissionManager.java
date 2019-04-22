package com.sunrain.timetablev4.manager.permission;

import android.app.Activity;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

public interface PermissionManager {


    void checkPermission(String[] permissions, int requestCode, @StringRes int hintMessageId, @StringRes int messageId);

    /**
     * @param hintMessage 如果有权限曾被拒绝，弹出提醒的文案
     * @param message     向系统申请权限前，给用户弹出提示
     */
    void checkPermission(String[] permissions, int requestCode, String hintMessage, String message);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    interface OnRequestPermissionsListener {

        void onPermissionGranted(int requestCode);

        void onPermissionDenied(int requestCode, boolean neverAskAgainChecked);
    }

    class Factory {

        public static PermissionManager get(Activity activity, PermissionManager.OnRequestPermissionsListener listener) {
            return new ActivityPermissionManager(activity, listener);
        }

        public static PermissionManager get(Fragment fragment, PermissionManager.OnRequestPermissionsListener listener) {
            return new FragmentPermissionManager(fragment, listener);
        }
    }
}