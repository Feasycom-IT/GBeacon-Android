package com.glowbird.gbeacon.ui.setting;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.util.Log;

public class AoABeaconAdvertiser {

    private BluetoothLeAdvertiser advertiser;

    @SuppressLint("MissingPermission")
    public void startAdvertising(Context context, short id, byte alarm, byte battery, byte mode, byte txPower, byte channel, byte isStatic) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // 设备不支持蓝牙或蓝牙未开启
            return;
        }

        if (!bluetoothAdapter.isMultipleAdvertisementSupported()) {
            // 设备不支持多广告
            return;
        }

        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            // 设备不支持 BLE 广告
            return;
        }

        byte[] bs = coreaiot_generate_manufacturer_specific_data(id, alarm, battery, mode, txPower, channel, isStatic);
        String str = byteArrayToHex(bs);
        Log.e("TAG","str => " + str);

        int manufacturer_id = coreaiot_get_manufacturer_id();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(mode)
                .setTxPowerLevel(txPower)
                .setConnectable(false)
                .setTimeout(0)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .addManufacturerData(manufacturer_id, bs).build();

        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // 广播开始
            Log.d("AoABeacon", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // 广播失败
            Log.e("AoABeacon", "Advertising start failed: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback);
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }

  /*  static {
        System.loadLibrary("rtls");
    }*/

    public native byte[] coreaiot_generate_manufacturer_specific_data(short id, byte alarm, byte battery, byte advertise_mode,
                                                                      byte tx_power_level, byte channel, byte is_static);
    public native int coreaiot_get_manufacturer_id();

}
