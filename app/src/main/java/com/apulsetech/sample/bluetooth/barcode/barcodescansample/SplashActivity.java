package com.apulsetech.sample.bluetooth.barcode.barcodescansample;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities.PermissionManager;

public class SplashActivity extends AppCompatActivity
        implements PermissionManager.OnResultPermissionListener {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int DELAY_SPLASH_TIME = 800;

    private PermissionManager permManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        permManager = new PermissionManager(this, this);
        checkPermission();

        Log.i(TAG, "INFO. onCreate()");
    }

    private void checkPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            permissions = new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        } else {
            permissions = new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
            };
        }
        permManager.checkPermission(permissions);

        Log.i(TAG, "INFO. checkPermission()");
    }

    private void startInstalledAppDetailActivity() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        Log.i(TAG, "INFO. startInstalledAppDetailActivity()");
    }

    @Override
    public void onGranted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                Log.i(TAG, "INFO. onGranted() - Start Main Activity");
            }
        }, DELAY_SPLASH_TIME);
        Log.i(TAG, "INFO. onGranted()");
    }

    @Override
    public void onDenied(String[] permissions) {

    }

    @Override
    public void onPermanentDenied(String[] permissions) {

    }
}