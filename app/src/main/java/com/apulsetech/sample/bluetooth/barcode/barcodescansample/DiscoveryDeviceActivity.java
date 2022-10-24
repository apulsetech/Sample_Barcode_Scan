package com.apulsetech.sample.bluetooth.barcode.barcodescansample;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.apulsetech.lib.event.ScannerEventListener;
import com.apulsetech.lib.remote.thread.RemoteController;
import com.apulsetech.lib.remote.type.Module;
import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.lib.barcode.Scanner;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.adapters.DeviceListAdapter;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.data.Const;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.dialogs.WaitDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class DiscoveryDeviceActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ScannerEventListener {

    private static final String TAG = DiscoveryDeviceActivity.class.getSimpleName();

    private static final int TIMEOUT = 30000;

    private ListView lstPairedDevices;
    private DeviceListAdapter adpPairedDevices;
    private ProgressBar pgbDiscoveringDevices;
    private ListView lstDisCoveringDevices;
    private DeviceListAdapter adpDisCoveringDevice;
    private Button btnAction;

    private BluetoothAdapter btAdapter;

    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        lstPairedDevices = findViewById(R.id.paired_devices);
        adpPairedDevices = new DeviceListAdapter();
        lstPairedDevices.setAdapter(adpPairedDevices);
        lstPairedDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstPairedDevices.setOnItemClickListener(this);

        pgbDiscoveringDevices = findViewById(R.id.discovering_progress);
        pgbDiscoveringDevices.setVisibility(View.GONE);

        lstDisCoveringDevices = findViewById(R.id.discovering_devices);
        adpDisCoveringDevice = new DeviceListAdapter();
        lstDisCoveringDevices.setAdapter(adpDisCoveringDevice);
        lstDisCoveringDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstDisCoveringDevices.setOnItemClickListener(this);

        btnAction = findViewById(R.id.action_discovering);
        btnAction.setOnClickListener(this);

        fillPairedDevices();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_discovering:
                btnAction.setEnabled(false);
                if(btAdapter.isDiscovering()) {
                    stopDiscovering();
                } else {
                    startDiscovering();
                }
                break;
        }
    }

    // 인텐트 필터 생성 및 블루투스 디스커버리 시작
    @SuppressLint("MissingPermission")
    private void startDiscovering() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(receiver, filter);

        adpDisCoveringDevice.clear();
        btAdapter.startDiscovery();
    }

    // 블루투스 디스커버리 종료
    @SuppressLint("MissingPermission")
    private void stopDiscovering() {
        if(btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
    }

    // 브로드캐스트 생성
    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // 외부에서 인텐트 받아옴
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // EXTRA_DEVICE이라는 인텐트를 받아 그 인텐트에 넘겨진 디바이스 get함.
                String name = device.getName();
                String address = device.getAddress();
                if (name != null && name.length() > 2) {
                    adpDisCoveringDevice.add(RemoteDevice.makeBtSppDevice(device));
                    adpDisCoveringDevice.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // 리스트 뷰 재초기화를 위해 데모보면 발견된 디바이스의 이름이 순간 바뀌는 것 처럼.
                String name = device.getName();
                String address = device.getAddress();
                if (name != null && name.length() > 2) {
                    if(name.substring(0,1).equals("a")) {
                        adpDisCoveringDevice.add(RemoteDevice.makeBtSppDevice(device));
                        adpDisCoveringDevice.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                pgbDiscoveringDevices.setVisibility(View.VISIBLE);
                btnAction.setText(R.string.action_stop_discovering);
                btnAction.setEnabled(true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                pgbDiscoveringDevices.setVisibility(View.GONE);
                btnAction.setText(R.string.action_start_discovering);
                btnAction.setEnabled(true);
                unregisterReceiver(receiver); // Receiver 해제
            }
        }
    };

    // 리스트에 추가된 디바이스 목록 클릭할 때 발생 클릭이벤트
    // parent 객체 밑 자식 객체들을 클릭했을때 발생하는 이벤트 처리
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RemoteDevice device = null;
        switch (parent.getId()) {
            case R.id.paired_devices:
                device = (RemoteDevice) adpPairedDevices.getItem(position);
                break;
            case R.id.discovering_devices:
                device = (RemoteDevice) adpDisCoveringDevice.getItem(position);
                break;
            default:
                return;
        }
        WaitDialog.show(this,getString(R.string.msg_connect_device));
        Scanner scanner = Scanner.getScanner(DiscoveryDeviceActivity.this, device, false, TIMEOUT);
        if(scanner != null) {
            final RemoteDevice finalDevice = device;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(scanner.start()) {
                        scanner.setEventListener(DiscoveryDeviceActivity.this);
                        boolean mIsRemoteDevice = scanner.isRemoteDevice();
                        if(mIsRemoteDevice) {
                            RemoteController controller = RemoteController.getRemoteControllerIfAvailable();
                            if(controller != null) {
                                controller.setRemoteDeviceTriggerActiveModule(Module.BARCODE);
                            }
                        }
                        // Connected device
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                Intent intent = new Intent();
                                intent.putExtra(Const.REMOTE_DEVICE, finalDevice);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    } else {
                        // Failed to start Barcode service
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WaitDialog.hide();
                                scanner.destroy();
                                finish();
                            }
                        });
                    }
                }
            }).start();
        } else {
            WaitDialog.hide();
        }
    }

    @SuppressLint("MissingPermission")
    private void fillPairedDevices(){
        adpPairedDevices.clear();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress();
                if(name != null && name.length() > 2) {
                    adpPairedDevices.add(RemoteDevice.makeBtSppDevice(device));
                    adpPairedDevices.notifyDataSetChanged();
                    Log.d(TAG, String.format(Locale.US, "DEBUG. PAIRED DEVICE [[%s], [%s]]",
                            name,address));
                }
            }
        }
        adpPairedDevices.notifyDataSetChanged();
    }
}
