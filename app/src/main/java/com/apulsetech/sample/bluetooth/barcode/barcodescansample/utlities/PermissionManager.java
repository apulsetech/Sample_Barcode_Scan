package com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PermissionManager {
    private static final String TAG = PermissionManager.class.getSimpleName();

    private static final String KEY_NO_FIRST_START = "no_first_start_app";

    private ComponentActivity activity;

    private ActivityResultLauncher<String[]> launcherPermissionResult;
    private OnResultPermissionListener listener;

    public PermissionManager(ComponentActivity activity, OnResultPermissionListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.launcherPermissionResult = this.activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        List<String> deniedPermission = new ArrayList<>();
                        for(Map.Entry<String, Boolean> entry : result.entrySet()) {
                            if (entry.getValue() == false) {
                                deniedPermission.add(entry.getKey());
                            }
                        }
                        if (deniedPermission.size() > 0) {
                            if (listener != null)
                                listener.onDenied(deniedPermission.toArray(new String[deniedPermission.size()]));
                        } else {
                            if (listener != null)
                                listener.onGranted();
                        }
                    }
                });
    }

    public void checkPermission(Collection<String> permissions) {
        List<String> checkPermissions = new ArrayList<>();
        List<String> reqPermissions = new ArrayList<>();
        boolean isNoFirstStarted = false;
        SharedPreferences pref =
                this.activity.getSharedPreferences(AppInfoUtil.getPackageName(activity),
                Context.MODE_PRIVATE);
        isNoFirstStarted = pref.getBoolean(KEY_NO_FIRST_START, false);
        if (isNoFirstStarted == false) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(KEY_NO_FIRST_START, true);
            editor.commit();
        }
        // Check Permissions
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                checkPermissions.add(permission);
                Log.d(TAG, String.format(Locale.US, "CHECK. %s", permission));
            }
        }
        if (checkPermissions.size() <= 0) {
            if (listener != null)
                listener.onGranted();
            return;
        }
        // Should show request permission rationale
        for (String permission : checkPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                reqPermissions.add(permission);
                Log.d(TAG, String.format(Locale.US, "REQUEST. %s", permission));
            }
        }
        if (reqPermissions.size() <= 0) {
            this.launcherPermissionResult.launch(
                    checkPermissions.toArray(new String[checkPermissions.size()]));
        } else {
            if (isNoFirstStarted) {
                if (listener != null)
                    listener.onPermanentDenied(
                            reqPermissions.toArray(new String[reqPermissions.size()]));
            } else {
                this.launcherPermissionResult.launch(
                        reqPermissions.toArray(new String[reqPermissions.size()]));
            }
        }
    }

    public void checkPermission(String[] permissions) {
        checkPermission(Arrays.asList(permissions));
    }

    public interface OnResultPermissionListener {
        void onGranted();
        void onDenied(String[] permissions);
        void onPermanentDenied(String[] permissions);
    }
}
