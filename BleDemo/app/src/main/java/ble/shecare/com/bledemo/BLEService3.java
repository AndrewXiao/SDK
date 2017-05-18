package ble.shecare.com.bledemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import ble.shecare.com.sdk.SDK3;

import static android.content.ContentValues.TAG;
import static ble.shecare.com.bledemo.BLEParam3.Ack_Char_UUID;
import static ble.shecare.com.bledemo.BLEParam3.DESC_CCC;
import static ble.shecare.com.bledemo.BLEParam3.Temp_Char_UUID;
import static ble.shecare.com.bledemo.BLEParam3.TemperServiceUuids;


public class BLEService3 extends Service {
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private MainActivity3 mActivity;

    private BluetoothDevice mDevice;

    @Override
    public void onCreate() {
        Log.i(TAG, "BLEService3 onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "BLEService3 onBind");
        return this.mBinder;
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        Log.i(TAG, "BLEService3 bindService");
        return super.bindService(service, conn, flags);
    }

    public class LocalBinder extends Binder {
        public BLEService3 getService() {
            return BLEService3.this;
        }
    }

    public void bleScan(MainActivity3 activity){
        mActivity = activity;

        initBLE();

        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            activity.startActivityForResult(enableBtIntent, 1);
        }else{
            mBluetoothAdapter.startLeScan(TemperServiceUuids, mLeScanCallback);
            Log.i("BLEService3", "正在扫描中...");
        }
    }

    public void bleStopScan(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    public void initBLE(){
        mBluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    /**
     * 1. 回调都是子线程，为了能方便的操作UI，广播出去，直接接收后操作UI。
     * 2. Activity， Service， Broadcast 解耦
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            mDevice = device;
            Log.i("BLEService3", "已扫描到设备! name = " + device.getName());

            Intent intent = new Intent(Receiver3.BLE_DEVICE_FOUND);
            intent.putExtra(Receiver3.EXTRA_DEVICE_NAME, device.getName());
            intent.putExtra(Receiver3.EXTRA_DEVICE_ADDR, device.getAddress());
            sendBroadcast(intent);
        }
    };

    public void connect(){
        mActivity.gatt = mDevice.connectGatt(mActivity, false, mGattCallback);
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Intent intent = new Intent(Receiver3.BLE_GATT_CONNECTED);
                sendBroadcast(intent);

                mActivity.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        gatt.discoverServices();
                    }
                });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Intent intent = new Intent(Receiver3.BLE_GATT_DISCONNECTED);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            List<BluetoothGattService> bleGattServiceList = gatt.getServices();
            for (BluetoothGattService bleGattService : bleGattServiceList) {
                Log.i(TAG, "发现服务: " +  bleGattService.getUuid());

                List<BluetoothGattCharacteristic> bleGattCharacteristicList = bleGattService.getCharacteristics();
                for (BluetoothGattCharacteristic bleGattCharacteristic : bleGattCharacteristicList) {
                    Log.i(TAG, "发现服务有以下特征: " +  bleGattCharacteristic.getUuid());

                    if(Temp_Char_UUID.equals(bleGattCharacteristic.getUuid().toString())) {
                        mActivity.tempCharacteristic = bleGattCharacteristic;
                        SDK3.requestIndication(gatt, mActivity.tempCharacteristic); // 这里必须加上，因为温度 是靠 onCharacteristicChanged 收到的!
                    } else if(Ack_Char_UUID.equals(bleGattCharacteristic.getUuid().toString())) {
                        mActivity.bleGattACKCharacteristic = bleGattCharacteristic;
                        SDK3.requestIndication(gatt, mActivity.bleGattACKCharacteristic);
                    } else if(BLEParam3.TIME_SYNC_UUID.equals(bleGattCharacteristic.getUuid().toString())){
                        mActivity.firmwareTimeSyncCharacteristic = bleGattCharacteristic;
                    } else if(BLEParam3.TEMP_UNIT_UUID.equals(bleGattCharacteristic.getUuid().toString())){
                        mActivity.bleGatt3GTempUnitCharacteristic = bleGattCharacteristic;
                        SDK3.requestIndication(gatt, mActivity.bleGatt3GTempUnitCharacteristic);
                    } else if(BLEParam3.FirmwareVersion_Char_UUID.equals(bleGattCharacteristic.getUuid().toString())){
                        mActivity.firmwareVersionCharacteristic = bleGattCharacteristic;
                    }
                }
            }

            mActivity.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mActivity.timeSyncBtn.setEnabled(true);
                    mActivity.firmwareVersionBtn.setEnabled(true);
                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // 说明 indication 已经成功, 此时可以发送指令
            if (DESC_CCC.equals(descriptor.getUuid())) {
                Log.i(TAG, "requestIndication 结束!");

                Intent intent = new Intent(Receiver3.BLE_DESCRIPTOR_WRITE);
                sendBroadcast(intent);
            }
        }

        // 在这里等待温度的接收
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte [] val = characteristic.getValue();

            Intent intent = new Intent(Receiver3.BLE_CHARACTERISTIC_CHANGED);
            intent.putExtra(Receiver3.EXTRA_BLE_UUID, characteristic.getUuid().toString());
            intent.putExtra(Receiver3.EXTRA_VALUE, val);

            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte [] val = characteristic.getValue();

            Intent intent = new Intent(Receiver3.BLE_CHARACTERISTIC_READ);
            String uuid = characteristic.getUuid().toString();
            intent.putExtra(Receiver3.EXTRA_BLE_UUID, uuid);
            intent.putExtra(Receiver3.EXTRA_VALUE, val);

            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Intent intent = new Intent(Receiver3.BLE_CHARACTERISTIC_WRITE);

            String uuid = characteristic.getUuid().toString();
            intent.putExtra(Receiver3.EXTRA_BLE_UUID, uuid);

            sendBroadcast(intent);
        }
    };

}
