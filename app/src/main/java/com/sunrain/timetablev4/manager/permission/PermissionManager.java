package com.sunrain.timetablev4.manager.permission;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.StringRes;

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

    class Permission {

        public static final String[] ALL_PERMISSION;
        public static final String[] START_PERMISSION;
        public static final String[] MAP_PERMISSION;
        public static final String[] WIFI_PERMISSION;

        static {

            START_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
            MAP_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            WIFI_PERMISSION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ALL_PERMISSION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BODY_SENSORS, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission
                        .GET_ACCOUNTS, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALENDAR, Manifest.permission
                        .READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                        .READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission
                        .RECEIVE_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECORD_AUDIO, Manifest.permission
                        .SEND_SMS, Manifest.permission.TRANSMIT_IR, Manifest.permission.USE_SIP, Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 6.0以上没有system alert window 权限
                ALL_PERMISSION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BODY_SENSORS, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission
                        .GET_ACCOUNTS, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALENDAR, Manifest.permission
                        .READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                        .READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission
                        .RECEIVE_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECORD_AUDIO, Manifest.permission
                        .SEND_SMS, Manifest.permission.TRANSMIT_IR, Manifest.permission.USE_SIP, Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ALL_PERMISSION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission.GET_ACCOUNTS, Manifest.permission
                        .PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CALL_LOG, Manifest
                        .permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.RECEIVE_SMS, Manifest
                        .permission.RECEIVE_WAP_PUSH, Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS, Manifest.permission
                        .SYSTEM_ALERT_WINDOW, Manifest.permission.USE_SIP, Manifest.permission.WRITE_CALENDAR, Manifest.permission
                        .WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else {
                ALL_PERMISSION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission.GET_ACCOUNTS, Manifest.permission
                        .PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CALL_LOG, Manifest
                        .permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.RECEIVE_SMS, Manifest
                        .permission.RECEIVE_WAP_PUSH, Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS, Manifest.permission
                        .SYSTEM_ALERT_WINDOW, Manifest.permission.USE_SIP, Manifest.permission.WRITE_CALENDAR, Manifest.permission
                        .WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }
    }
}