package ble.shecare.com.bledemo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ble.shecare.com.sdk.SDK3;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity3 extends AppCompatActivity {

    public Handler mHandler;

    private static final String TAG = "BLELOG";

    @BindView(R.id.scanBtn)
    Button scanBtn;
    @BindView(R.id.connBtn)
    Button connBtn;
    @BindView(R.id.cmdBtn)
    Button cmdBtn;
    @BindView(R.id.actionBtn)
    LinearLayout actionBtn;
    @BindView(R.id.deviceName)
    TextView deviceName;
    @BindView(R.id.connState)
    TextView connState;
    @BindView(R.id.timeSyncBtn)
    Button timeSyncBtn;
    @BindView(R.id.unitSyncBtn)
    Button unitSyncBtn;
    @BindView(R.id.powerVolBtn)
    Button powerVolBtn;


    @BindView(R.id.timeSyncTv)
    TextView timeSyncTv;
    @BindView(R.id.unitSyncTv)
    TextView unitSyncTv;
    @BindView(R.id.powerVolTv)
    TextView powerVolTv;
    @BindView(R.id.temperTv)
    TextView temperTv;

    @BindView(R.id.firmwareVersionBtn)
    Button firmwareVersionBtn;
    @BindView(R.id.firmwareVersionTv)
    TextView firmwareVersionTv;

    @BindView(R.id.displayZone)
    LinearLayout displayZone;
    @BindView(R.id.activity_main)
    RelativeLayout activityMain;

    public BluetoothGatt gatt;
    public BluetoothGattCharacteristic tempCharacteristic;
    public BluetoothGattCharacteristic bleGattACKCharacteristic;
    public BluetoothGattCharacteristic firmwareTimeSyncCharacteristic;
    public BluetoothGattCharacteristic bleGatt3GTempUnitCharacteristic;
    public BluetoothGattCharacteristic firmwareVersionCharacteristic;

    private Receiver3 mReceiver3;
    public BLEService3 mService3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity3 bindService");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ButterKnife.bind(this);

        mHandler = new Handler();

        mReceiver3 = new Receiver3(this);
        registerReceiver(mReceiver3, Receiver3.getIntentFilter());

        Intent bindIntent = new Intent(this, BLEService3.class);
        bindService(bindIntent, mServiceConnection3, Context.BIND_AUTO_CREATE);

    }

    private final ServiceConnection mServiceConnection3 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            Log.i(TAG, "MainActivity3 onServiceConnected");
            mService3 = ((BLEService3.LocalBinder) rawBinder).getService();

            scanBtn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mService3 = null;
        }
    };

    @OnClick({R.id.scanBtn, R.id.connBtn, R.id.cmdBtn, R.id.timeSyncBtn, R.id.unitSyncBtn, R.id.powerVolBtn, R.id.firmwareVersionBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanBtn:
                mService3.bleScan(MainActivity3.this);
                break;
            case R.id.connBtn:
                mService3.connect();
                break;
            case R.id.timeSyncBtn:
                SDK3.syncTime(gatt, firmwareTimeSyncCharacteristic);
                break;
            case R.id.unitSyncBtn:
                SDK3.syncTempUnit(gatt, bleGatt3GTempUnitCharacteristic, true);
                break;
            case R.id.powerVolBtn:
                SDK3.requestBatteryVolume(gatt, bleGattACKCharacteristic);
                break;
            case R.id.cmdBtn:
                cmdBtn.setEnabled(false);
                SDK3.requestTemp(gatt, bleGattACKCharacteristic);
                break;
            case R.id.firmwareVersionBtn:
                SDK3.requestFirmVer(gatt, firmwareVersionCharacteristic);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver3);
        unbindService(mServiceConnection3);

    }
}
