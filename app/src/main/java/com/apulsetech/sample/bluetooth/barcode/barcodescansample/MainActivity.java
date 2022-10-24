package com.apulsetech.sample.bluetooth.barcode.barcodescansample;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.apulsetech.lib.barcode.Scanner;
import com.apulsetech.lib.barcode.type.BarcodeType;
import com.apulsetech.lib.event.DeviceEvent;
import com.apulsetech.lib.event.ScannerEventListener;
import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.adapters.BarcodeListAdapter;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.data.Const;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.dialogs.WaitDialog;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities.AppInfoUtil;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.utlities.PermissionManager;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements PermissionManager.OnResultPermissionListener ,
        ScannerEventListener {

    private static final String PREF_NAME = "barcode_scanner_sample";
    private static final String LAST_ADDRESS = "last_dev_address";

    private static final int DEFAULT_DECODE_INTERVAL = 1000;

    private static final int TIMEOUT = 30000;

    private boolean mHandsfreeEnabled = false;
    private boolean mFilterEnabled = true;

    private MenuItem mnuDiscoveryDevice = null;
    private MenuItem mnuReconnect = null;
    private MenuItem mnuDisconnect = null;

    private RemoteDevice mLastDevice = null;
    private BluetoothAdapter btAdapter;
    private Scanner mScanner = null;

    private Button mClearButton;
    private Button mScanButton;
    private Button mSymbologyButton;

    private PermissionManager permManager;

    private BarcodeListAdapter mBarcodeListAdapter;
    private ListView mBarcodeListView;

    private TextView txtVersion;
    private TextView txtConnState;
    private TextView txtAllCount;
    private TextView txtCount;

    private boolean mAutoTriggerEnabled = false;
    private boolean mAutoTriggerStarted = false;
    private boolean mAutoTriggerNeedRestart = false;
    private boolean mHoldTriggerEnabled = true;




    private int mAutoDecodeInterval = DEFAULT_DECODE_INTERVAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mBarcodeListView = (ListView)findViewById(R.id.Scan_list);
        mBarcodeListAdapter = new BarcodeListAdapter(this);
        mBarcodeListView.setAdapter(mBarcodeListAdapter);
        mBarcodeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mScanButton = (Button) findViewById(R.id.action_scan);
        mScanButton.setOnClickListener(mButtonClickListener);
        mClearButton = (Button)findViewById(R.id.action_clear);
        mClearButton.setOnClickListener(mButtonClickListener);
        mSymbologyButton = (Button)findViewById(R.id.action_symbol);
        mSymbologyButton.setOnClickListener(mButtonClickListener);

        txtVersion = (TextView) findViewById(R.id.version);
        txtVersion.setText(AppInfoUtil.getVersion(this));
        txtConnState = (TextView) findViewById(R.id.connect_state);
        txtConnState.setText(R.string.connection_state_disconnected);

        txtAllCount = (TextView) findViewById(R.id.all_count);
        txtCount = (TextView) findViewById(R.id.overlap_count);

        permManager = new PermissionManager(this,this);
        checkPermissions();
        initBluetooth();

        updateCount();

        enableWidgets(true);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mScanner = Scanner.getScanner(this);
        if (mScanner != null) {
            mScanner.setEventListener(this);
        }
    }

    @Override
    protected void onPause() {

        if (mScanner != null) {
            mScanner.removeEventListener(this);

            if (mAutoTriggerEnabled) {
                if (mAutoTriggerStarted) {
                    mAutoTriggerStarted = false;
                    if (mScanner.isDecoding()) {
                        mScanner.stopDecode();
                    }
                    enableScanButton(true);
                }
            } else {
                if (mScanner.isDecoding()) {
                    mScanner.stopDecode();
                    enableScanButton(true);
                }
            }
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mScanner != null) {
            if(mScanner.isDecoding()){
                mScanner.stopDecode();
            }
            mScanner.stop();
            mScanner.destroy();
            mScanner = null;
            txtAllCount.setText(R.string.connection_state_disconnected);
        }
        super.onDestroy();
    }

    private void checkPermissions() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        mnuDiscoveryDevice = menu.findItem(R.id.discovering_devices);
        mnuReconnect = menu.findItem(R.id.reconnect);
        mnuDisconnect = menu.findItem(R.id.disconnect);
        enableWidgets(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.discovery_device:
                showDiscoveryDevice();
                break;
            case R.id.disconnect:
                actionDisconnect();
                break;
            case R.id.reconnect:
                actionConnect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void actionDisconnect() {
        if (mScanner != null) {
            WaitDialog.show(this, getString(R.string.msg_disconnect_device));
            if(mScanner.isDecoding()) {
                mScanner.stopDecode();
            }
            mScanner.stop();
            mScanner.destroy();
            mScanner = null;
            enableWidgets(true);
            WaitDialog.hide();
        }
    }

    private void actionConnect() {
        WaitDialog.show(this, getString(R.string.msg_connect_device));
        mScanner = Scanner.getScanner(this, mLastDevice, false, TIMEOUT);
        if(mScanner != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(mScanner.start()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                mScanner.setEventListener(MainActivity.this);
                                enableWidgets(true);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                mScanner = null;
                                enableWidgets(true);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void showDiscoveryDevice() {
        Intent intent = new Intent(this, DiscoveryDeviceActivity.class);
        launcherDiscoveringResult.launch(intent);
    }

    private ActivityResultLauncher<Intent> launcherDiscoveringResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if((mLastDevice = result.getData().getParcelableExtra(Const.REMOTE_DEVICE)) != null) {
                        saveConfig();
                        if (mScanner == null) {
                            mScanner = Scanner.getScanner(MainActivity.this);
                        }
                    }
                } else {
                    if (mScanner != null) {
                        if (mScanner.isConnected()) {
                            mScanner.stopDecode();
                        }
                        mScanner.stop();
                    }
                    mScanner = null;
                }
                enableWidgets(true);
            });

    private void initBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isEnabled()) {
            enableWidgets(true);
        } else {
            showRequestEnableBluetooth();
        }
    }

    private void showRequestEnableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        enableWidgets(true);
                    }
                });
        launcher.launch(intent);
    }

    private void updateCount() {
        txtAllCount.setText(String.format(Locale.US, "%d", mBarcodeListAdapter.getCount()));
        txtCount.setText(String.format(Locale.US, "%d", mBarcodeListAdapter.getTotalCount()));
    }

    private void enableWidgets(boolean enabled) {

        boolean isBluetooth = false;
        boolean isDisconnected = false;
        boolean isConnected = false;

        if(btAdapter != null)
            isBluetooth = btAdapter.isEnabled();
        isDisconnected = mScanner == null;
        if(mScanner != null) {
            isConnected = mScanner.isConnected();
        }

        if(mnuDiscoveryDevice != null) {
            mnuDiscoveryDevice.setVisible(enabled && isBluetooth && isDisconnected);}
        if(mnuReconnect != null)
            mnuReconnect.setVisible(enabled && isBluetooth && isDisconnected && mLastDevice != null);
        if(mnuDisconnect != null)
            mnuDisconnect.setVisible(enabled && isBluetooth && !isDisconnected && isConnected);

        mScanButton.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected);
        mClearButton.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected);
        mSymbologyButton.setEnabled(enabled && isBluetooth && !isDisconnected && isConnected);
        txtConnState.setText(isDisconnected ?
                R.string.connection_state_disconnected :
                (isConnected ?
                        R.string.connection_state_connected :
                        R.string.connection_state_connecting));
    }

    private void saveConfig() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor writer = pref.edit();
        String addrss;
        if (mLastDevice == null) {
            addrss = "";
        } else {
            addrss = mLastDevice.getAddress();
        }
        writer.putString(LAST_ADDRESS, addrss);
        writer.commit();
    }

    private final View.OnClickListener mButtonClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.action_scan) {
                enableScanButton(false);
                toggleScan();
                enableScanButton(true);
            }else if(id == R.id.action_clear) {
                clearBarcodeList();
                updateCount();
            }else if(id == R.id.action_symbol) {

            }
        }
    };

    private void enableScanButton(boolean enabled) {
        if (enabled) {
            if(mScanner.isDecoding()) {
                mScanButton.setText(R.string.barcode_scan_button_text_stop_scan);
                mScanButton.setTextColor(getResources().
                        getColor(R.color.color_button_control_stop));
            }else {
                mScanButton.setText(R.string.barcode_scan_button_text_start_scan);
                mScanButton.setTextColor(getResources().
                        getColor(R.color.color_button_control_start));
            }
        }
        mScanButton.setEnabled(enabled);
    }

    @Override
    public void onScannerDeviceStateChanged(DeviceEvent status) {
        switch(status) {
            case CONNECTED:
                txtConnState.setText(R.string.connection_state_connected);
            case DISCONNECTED:
                txtConnState.setText(R.string.connection_state_disconnected);
                break;
        }
    }

    @Override
    public void onScannerEvent(BarcodeType type, String barcode) {
        String data = (barcode != null) ? barcode.trim() : null;

        if (type != BarcodeType.NO_READ) {
            int position = mBarcodeListAdapter.addItem(type, data, mFilterEnabled);
            mBarcodeListView.setSelection(position);
            mClearButton.setEnabled(true);
            updateCount();
        }
    }

    private void toggleScan() {
        if(mAutoTriggerEnabled) {
            if(mScanner.isDecoding()) {
                mScanner.stopDecode();
                enableWidgets(true);
            }
            else {
                mScanner.startDecode(false);
                enableWidgets(false);
                mAutoTriggerStarted = true;
            }
        }else {
            if (mScanner.isDecoding()) {
                mScanner.stopDecode();
                enableWidgets(true);
            } else {
                mScanner.startDecode(true);
                enableWidgets(false);
            }
        }
    }

    private void clearBarcodeList() {
        mBarcodeListAdapter.clear();
        mClearButton.setEnabled(false);
    }


    @Override
    public void onScannerRemoteKeyEvent(int action, int keyCode) {

        if ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_F7)) {
            if (action == KeyEvent.ACTION_DOWN) {
                processKeyDown();
            } else if (action == KeyEvent.ACTION_UP) {
                processKeyUp();
            }
        }
    }

    private void processKeyUp() {

        if (mScanner.isDecoding() &&
                !mHandsfreeEnabled &&
                !mAutoTriggerEnabled &&
                !mHoldTriggerEnabled) {
            mScanner.stopDecode();
        }

        enableScanButton(true);
    }


    private void processKeyDown() {

        enableScanButton(false);

        if (mAutoTriggerEnabled) {
            if (mAutoTriggerStarted) {
                mAutoTriggerStarted = false;
                if (mScanner.isDecoding()) {
                    mScanner.stopDecode();
                }
            } else {
                mScanner.startDecode(false);
                mAutoTriggerStarted = true;
            }
        } else {
            if (mScanner.isDecoding()) {
                mScanner.stopDecode();
            } else {
                mScanner.startDecode(mHandsfreeEnabled);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_F7)) &&
                (event.getRepeatCount() <= 0)) {

            processKeyDown();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_F7)) {

            processKeyUp();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onGranted() {

    }

    @Override
    public void onDenied(String[] permissions) {

    }

    @Override
    public void onPermanentDenied(String[] permissions) {

    }
}