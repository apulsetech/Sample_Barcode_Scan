package com.apulsetech.sample.bluetooth.barcode.barcodescansample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class GlobalKeyReceiver extends BroadcastReceiver {
    private static final String TAG = GlobalKeyReceiver.class.getSimpleName();

    private static final String ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON";
    private static final String ACTION_BUTTON_DOWN = "com.apulsetech.action.BUTTON_DOWN";
    private static final String ACTION_BUTTON_UP = "com.apulsetech.action.BUTTON_UP";

    private static final int TAG_ORIGIN_UNIFIED_DEMO = 1;
    private static final String KEY_ORIGIN = "origin";
    private static final String KEY_CODE = "key_code";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_GLOBAL_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            int keyCode = event.getKeyCode();
            int keyAction = event.getAction();

            if (event.getRepeatCount() > 0) {
                return;
            }

            if (keyAction == KeyEvent.ACTION_DOWN) {
                Intent downIntent = new Intent(ACTION_BUTTON_DOWN);
                downIntent.putExtra(KEY_ORIGIN, TAG_ORIGIN_UNIFIED_DEMO);
                downIntent.putExtra(KEY_CODE, keyCode);
                context.sendBroadcast(downIntent);
            } else if (keyAction == KeyEvent.ACTION_UP) {
                Intent upIntent = new Intent(ACTION_BUTTON_UP);
                upIntent.putExtra(KEY_ORIGIN, TAG_ORIGIN_UNIFIED_DEMO);
                upIntent.putExtra(KEY_CODE, keyCode);
                context.sendBroadcast(upIntent);
            }
        }
    }
}
