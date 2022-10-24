package com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Locale;

public class AppInfoUtil {
    private static final String TAG = AppInfoUtil.class.getSimpleName();

    /**
     * App ID 가져오기
     * @param context context   // @param: 변수 설명
     * @return APP ID   // @retrun 리턴 값 설명
     */
    public static String getAppId(Context context) {
        String appId = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            appId = pi.applicationInfo.loadDescription(pm) + "";
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. getAppId() - Failed to get app ID [%s]",
                    ex.getMessage()), ex);
            return "";
        }
        return appId;
    }

    /**
     * app name 가져오기
     * @param context context
     * @return appName
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo i = pm.getPackageInfo(context.getPackageName(), 0);
            appName = i.applicationInfo.loadLabel(pm) + "";
        } catch(PackageManager.NameNotFoundException ex) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. getAppName() - Failed to get app name [%s]",
                    ex.getMessage()), ex);
            return "";
        }
        return appName;
    }

    /**
     * package name 가져오기
     * @param context
     * @return packageName
     */
    public static String getPackageName(Context context) {
        String packageName = ""; // 패키지명 예시 데이터
        try {
            PackageManager packagemanager = context.getPackageManager();
            ApplicationInfo appinfo = packagemanager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            packageName = packagemanager.getApplicationLabel(appinfo).toString();
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. getPackageName() - Failed to get package name [%s]",
                    ex.getMessage()), ex);
            return "";
        }
        return packageName;
    }

    /**
     * app version 가져오기
     * @param context context
     * @return versionName
     */
    public static String getVersion(Context context) {
        String versionName = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName + "";
        } catch(PackageManager.NameNotFoundException ex) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. getVersion() - Failed to get app version [%s]",
                    ex.getMessage()), ex);
            return "";
        }
        return versionName;
    }

    /**
     * app version code 가져오기
     * @param context context
     * @return versionCode
     */
    public static int getVersionCode(Context context) {
        int versionCode = 1;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = i.versionCode;
        } catch(PackageManager.NameNotFoundException ex) {
            Log.e(TAG, String.format(Locale.US,
                    "ERROR. getVersionCode() - Failed to get app version code [%s]",
                    ex.getMessage()), ex);
            return 0;
        }
        return versionCode;
    }
}
