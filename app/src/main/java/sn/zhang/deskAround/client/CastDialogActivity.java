package sn.zhang.deskAround.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.Editable;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CastDialogActivity extends AppCompatActivity {
    private TextView castingTextView;
    private Intent pendingCastIntent;
    private BluetoothDevice pendingCastDevice;
    private CastEndpointManager manager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private long startProcessTime;
    private long endProcessTime;
    private long startScanTime;
    private long stopScanTime;
    private ScanCallback castScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothLeScanner.stopScan(castScanCallback);
            isScanning = false;
            stopScanTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "target device found in " + (stopScanTime - startScanTime), Toast.LENGTH_SHORT).show();
            connectAndCast(result.getDevice());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            updateStatus("扫描失败：" + errorCode);
            isScanning = false;
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Messager.ACTION_TARGET_CONNECTED:
                    updateStatus("正在投送到 " + manager.deviceName);
                    manager.cast();
                    break;
                case Messager.ACTION_CAST_DONE:
                    updateStatus("已投送到 " + manager.deviceName);
                    endProcessTime = System.currentTimeMillis();
                    Toast.makeText(getApplicationContext(), "cast finished in " + (endProcessTime - startProcessTime), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> finish(), 1500);
                    break;
                case Messager.ACTION_CAST_FAILED:
                    updateStatus("投送失败：" + intent.getStringExtra(Messager.EXTRA_DATA));
                    break;
                case Messager.ACTION_CONTENT_PREPARATION_REQUIRED:
                    prepareCastContent();
                    break;
                case Messager.ACTION_CONTENT_PREPARATION_FINISHED:
                    manager.cast();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startProcessTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cast_demo);
        //BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        //bottomSheetDialog.setContentView(R.layout.dialog_cast_demo);
        //bottomSheetDialog.show();
        //castingTextView = bottomSheetDialog.findViewById(R.id.casting_textview);
        castingTextView = findViewById(R.id.casting_textview);
        handleNfcAction(getIntent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 0) {
                castByIntent(pendingCastIntent);
            } else if (requestCode == 1) {
                connectAndCast(pendingCastDevice);
            }
        } else {
            updateStatus("投送失败：已拒绝蓝牙权限");
        }
    }

    private void updateStatus(String text) {
        castingTextView.setText(text);
    }

    private void prepareCastContent(String content) {
        manager.content = content;
        manager.isPrepared = true;
        Messager.announce(this, Messager.ACTION_CONTENT_PREPARATION_FINISHED);
    }

    private void prepareCastContent() {
        setTheme(R.style.Theme_TapShareClient);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.sheet_fill_demo_cast_message);
        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.findViewById(R.id.continue_cast_button).setOnClickListener(view1 -> {
            Editable message = ((TextInputEditText) bottomSheetDialog.findViewById(R.id.demo_cast_message_edittext)).getText();
            if (message != null && message.length() != 0) {
                manager.content = message.toString();
            }
            manager.isPrepared = true;
            Messager.announce(this, Messager.ACTION_CONTENT_PREPARATION_FINISHED);
            bottomSheetDialog.dismiss();
        });
    }

    private void castByIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                updateStatus("以蓝牙投送需要授予权限");
                pendingCastIntent = intent;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                return;
            }
        }
        //manager = new CastEndpointManager(this, intent.getData().getHost(), intent.getData().getLastPathSegment());
        manager = new CastEndpointManager(this, intent.getData());
        bluetoothAdapter = getSystemService(BluetoothManager.class).getAdapter();
        if (bluetoothAdapter == null) {
            updateStatus("bluetoothAdapter is null!");
            return;
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            updateStatus("bluetoothLeScanner is null!");
            return;
        }
        updateStatus("正在寻找 " + manager.castTargetServiceUUID);
        isScanning = true;
        new Handler().postDelayed(() -> {
            if (isScanning) {
                updateStatus("附近无目标设备");
                isScanning = false;
                bluetoothLeScanner.stopScan(castScanCallback);
            }
        }, 15000);
        ScanSettings settings = new ScanSettings.Builder().setLegacy(false).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(manager.castTargetServiceUUID)).build());
        startScanTime = System.currentTimeMillis();
        bluetoothLeScanner.startScan(filters, settings, castScanCallback);
    }

    private void connectAndCast(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                updateStatus("以蓝牙投送需要授予权限");
                pendingCastDevice = device;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }
        }
        updateStatus("准备投送到 " + device.getName());
        manager.connect(device).retry(3, 100).timeout(15000).useAutoConnect(true)
                .before(device1 -> updateStatus("正在连接 " + device1.getName()))
                .done(device1 -> {
                    updateStatus("已连接到 " + device1.getName() + "，正在开始投送");
                    Messager.announce(this, Messager.ACTION_TARGET_CONNECTED);
                })
                .fail((device1, status) -> updateStatus("无法连接到 " + device.getName() + "：" + status))
                .enqueue();
    }

    private void handleNfcAction(Intent intent) {
        //if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) && "castto".equals(intent.getScheme())) {
        if ("castto".equals(intent.getScheme())) {
            castByIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, Messager.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNfcAction(intent);
    }
}
