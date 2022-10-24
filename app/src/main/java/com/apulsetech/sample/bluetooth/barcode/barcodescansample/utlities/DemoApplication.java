package com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.apulsetech.lib.provider.LocalStateDBHelper;
import com.apulsetech.lib.util.LogUtil;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.MainActivity;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.type.CustomContext;

public class DemoApplication extends Application  {
    private static final String TAG = "DemoApplication";
    private static final boolean D = true;
    private LocalStateDBHelper mStateDBHelper;

    @Override
    public void onCreate() {

        super.onCreate();

        LocalStateDBHelper.setAuthorities("com.apulsetech.sample.bluetooth.barcode.barcodescansample.LocalStateProvider");
        mStateDBHelper = new LocalStateDBHelper(this);
        mStateDBHelper.reset();

        registerReceiver(mApplicationReceiver, new IntentFilter(CustomContext.ACTION_REMOTE_CONNECTION_REQUESTED));
    }

    private final BroadcastReceiver mApplicationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int barcodeRunState = mStateDBHelper.getBarcodeRunState();
            boolean barcodeRuning = barcodeRunState != 0;

            LogUtil.log(LogUtil.LV_D, D, TAG,
                    "runState=" + barcodeRuning);

            if(barcodeRuning) {
                LogUtil.log(LogUtil.LV_D, D, TAG, "Close the function module.");
                Intent baseActivityIntent = new Intent(DemoApplication.this, MainActivity.class);
                baseActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(baseActivityIntent);
            }

        }
    };
}
