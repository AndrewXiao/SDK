package ble.shecare.com.bledemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import ble.shecare.com.sdk.SDK3;

import static ble.shecare.com.bledemo.BLEParam3.Ack_Char_UUID;
import static ble.shecare.com.bledemo.BLEParam3.FirmwareVersion_Char_UUID;
import static ble.shecare.com.bledemo.BLEParam3.TEMP_UNIT_UUID;
import static ble.shecare.com.bledemo.BLEParam3.TIME_SYNC_UUID;
import static ble.shecare.com.bledemo.BLEParam3.Temp_Char_UUID;


public class Receiver3 extends BroadcastReceiver {
    private MainActivity3 activity;
    private static final String TAG = "BLE";
    private String showTempStr = "";

    public static final String BLE_DEVICE_FOUND = "com.ble.device_found";
    public static final String BLE_GATT_CONNECTED = "com.ble.gatt_connected";
    public static final String BLE_GATT_DISCONNECTED = "com.ble.gatt_disconnected";
    public static final String BLE_CHARACTERISTIC_READ = "com.ble.characteristic_read";
    public static final String BLE_CHARACTERISTIC_WRITE = "com.ble.characteristic_write";
    public static final String BLE_CHARACTERISTIC_CHANGED = "com.ble.characteristic_changed";
    public static final String BLE_DESCRIPTOR_WRITE = "com.ble.descriptor_write";

    public static final String EXTRA_DEVICE_NAME = "DEVICE";
    public static final String EXTRA_DEVICE_ADDR = "ADDRESS";
    public static final String EXTRA_BLE_UUID = "UUID";
    public static final String EXTRA_VALUE = "VALUE";


    public Receiver3(Activity activity) {
        this.activity = (MainActivity3) activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.i(TAG, "STATE_OFF 手机蓝牙关闭");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.i(TAG, "STATE_TURNING_OFF 手机蓝牙正在关闭");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i(TAG, "STATE_ON 手机蓝牙开启");
                    activity.scanBtn.setEnabled(true);
                    activity.mService3.bleScan(activity);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i(TAG, "STATE_TURNING_ON 手机蓝牙正在开启");
                    break;
            }
        }else if (BLE_DEVICE_FOUND.equals(action)) {
            activity.deviceName.setText("设备名称：" + intent.getStringExtra(EXTRA_DEVICE_NAME) + "\n MAC 地址：" + intent.getStringExtra(EXTRA_DEVICE_ADDR));
            activity.connBtn.setEnabled(true);
            activity.scanBtn.setEnabled(false);
            activity.connState.setText("未连接");

            activity.mService3.bleStopScan();
        } else if (BLE_GATT_CONNECTED.equals(action)) {
            Log.i(TAG, "MainActivity3 已连接!");

            activity.connState.setText("连接状态：已连接");
            activity.connBtn.setEnabled(false);

            showTempStr = "";
        } else if (BLE_GATT_DISCONNECTED.equals(action)) {
            Log.i(TAG, "MainActivity3 断开连接!");

            activity.connState.setText("连接状态：未连接");
            activity.connBtn.setEnabled(true);

        } else if(BLE_CHARACTERISTIC_WRITE.equals(action)){
            String uuid = intent.getStringExtra(EXTRA_BLE_UUID);

            if (BLEParam3.TIME_SYNC_UUID.equals(uuid)) {
                Log.i(TAG, "BLE_CHARACTERISTIC_WRITE! 开始时间同步, 第二阶段!");
                SDK3.requestIndication(activity.gatt, activity.firmwareTimeSyncCharacteristic);
            }
        } else if (BLE_CHARACTERISTIC_READ.equals(action)) {
            Log.i(TAG, "MainActivity3 获取固件版本号!");
            byte[] val = intent.getExtras().getByteArray(EXTRA_VALUE);
            String uuid = intent.getStringExtra(EXTRA_BLE_UUID);

            if(FirmwareVersion_Char_UUID.equals(uuid)) {
                String firmVer = SDK3.getFirmVer(val);
                activity.firmwareVersionTv.setText("固件版本 = " + firmVer);
                Log.i(TAG, "MainActivity3 固件版本 = " + firmVer);
            }
        } else if (BLE_CHARACTERISTIC_CHANGED.equals(action)){ // 一条温度一个change
            byte[] val = intent.getExtras().getByteArray(EXTRA_VALUE);
            String uuid = intent.getStringExtra(EXTRA_BLE_UUID);

            if(TIME_SYNC_UUID.equals(uuid)) {
                Log.i(TAG, "BLE_CHARACTERISTIC_INDICATION! 时间同步完毕!");
                activity.timeSyncTv.setText("时间同步完毕!");

                activity.timeSyncBtn.setEnabled(false);
                activity.unitSyncBtn.setEnabled(true);
            } else if(TEMP_UNIT_UUID.equals(uuid)) {
                if(SDK3.syncTempUnitSucc(val)) {
                    Log.i(TAG, "设置三代设备温度类型成功!");
                    activity.unitSyncTv.setText("设置三代设备温度类型成功!");

                    activity.unitSyncBtn.setEnabled(false);
                    activity.powerVolBtn.setEnabled(true);
                } else {
                    Log.i(TAG, "设置三代设备温度类型失败!");
                    activity.unitSyncTv.setText("设置三代设备温度类型失败!");
                }
            } else if(Ack_Char_UUID.equals(uuid)) {

                int batteryVolume = SDK3.getBatteryVolume(val);
                if(batteryVolume != 0) {
                    activity.powerVolTv.setText("电量值:" + batteryVolume);

                    activity.powerVolBtn.setEnabled(false);
                    activity.cmdBtn.setEnabled(true);
                }

            }else if(Temp_Char_UUID.equals(uuid)) {
                Log.i(TAG, "收到温度!");
                if(SDK3.isTempRecComplete(activity.gatt, activity.bleGattACKCharacteristic, val)){
                    Map<String, Double> temps = SDK3.getRecTemps();

                    String result = "";
                    Set<Map.Entry<String, Double>> entrySet = temps.entrySet();
                    for(Map.Entry<String, Double> entry : entrySet)
                        result += "收到温度: 时间 = " + entry.getKey() + " 温度 = " + entry.getValue() + " \n";

                    activity.temperTv.setText(result);
                }
            }
        }
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BLE_DEVICE_FOUND);
        intentFilter.addAction(BLE_GATT_CONNECTED);
        intentFilter.addAction(BLE_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_DESCRIPTOR_WRITE);
        intentFilter.addAction(BLE_CHARACTERISTIC_READ);
        intentFilter.addAction(BLE_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BLE_CHARACTERISTIC_CHANGED);

        return intentFilter;
    }
}
