package com.poupa.vinylmusicplayer.ui.activities.bugreport.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.IntRange;

import java.util.Arrays;

public class DeviceInfo {
    private final int versionCode;
    private final String versionName;
    private final String buildVersion = Build.VERSION.INCREMENTAL;
    private final String releaseVersion = Build.VERSION.RELEASE;
    @IntRange(from = 0)
    private final int sdkVersion = Build.VERSION.SDK_INT;
    private final String buildID = Build.DISPLAY;
    private final String brand = Build.BRAND;
    private final String manufacturer = Build.MANUFACTURER;
    private final String device = Build.DEVICE;
    private final String model = Build.MODEL;
    private final String product = Build.PRODUCT;
    private final String hardware = Build.HARDWARE;
    @SuppressLint("NewApi")
    private final String[] abis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
            Build.SUPPORTED_ABIS : new String[]{Build.CPU_ABI, Build.CPU_ABI2};
    @SuppressLint("NewApi")
    private final String[] abis32Bits = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
            Build.SUPPORTED_32_BIT_ABIS : null;
    @SuppressLint("NewApi")
    private final String[] abis64Bits = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
            Build.SUPPORTED_64_BIT_ABIS : null;

    public DeviceInfo(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo != null) {
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } else {
            versionCode = -1;
            versionName = null;
        }
    }

    @Override
    public String toString() {
        return "App version: " + versionName + "\n"
                + "App version code: " + versionCode + "\n"
                + "Android build version: " + buildVersion + "\n"
                + "Android release version: " + releaseVersion + "\n"
                + "Android SDK version: " + sdkVersion + "\n"
                + "Android build ID: " + buildID + "\n"
                + "Device brand: " + brand + "\n"
                + "Device manufacturer: " + manufacturer + "\n"
                + "Device name: " + device + "\n"
                + "Device model: " + model + "\n"
                + "Device product name: " + product + "\n"
                + "Device hardware name: " + hardware + "\n"
                + "ABIs: " + Arrays.toString(abis) + "\n"
                + "ABIs (32bit): " + Arrays.toString(abis32Bits) + "\n"
                + "ABIs (64bit): " + Arrays.toString(abis64Bits);
    }
}
