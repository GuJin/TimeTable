package com.sunrain.timetablev4.manager.permission;

import android.content.pm.PackageManager;

abstract class BasePermissionManager implements PermissionManager {

    boolean isGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
