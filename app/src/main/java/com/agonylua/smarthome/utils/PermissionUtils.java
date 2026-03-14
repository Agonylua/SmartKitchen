package com.agonylua.smarthome.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    /**
     * 获取蓝牙配网和定位所需的权限集合 (兼容 Android 12+)
     */
    public static String[] getBluetoothAndLocationPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要专门的蓝牙扫描和连接权限
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        // 无论哪个版本，蓝牙扫描都需要精确定位权限
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        return permissions.toArray(new String[0]);
    }

    /**
     * 获取扫码所需的相机权限
     */
    public static String[] getCameraPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    /**
     * 检查给定的权限组是否已经全部被授予
     */
    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context == null || permissions == null) return false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 过滤并返回当前还没有被授予的权限列表
     */
    public static String[] getDeniedPermissions(Context context, String[] permissions) {
        if (context == null || permissions == null) return new String[0];
        List<String> denied = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                denied.add(permission);
            }
        }
        return denied.toArray(new String[0]);
    }
}