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

public class AltBeaconAdvertiser {

    private BluetoothLeAdvertiser advertiser;

    @SuppressLint("MissingPermission")
    public void startAdvertising(Context context, String id1, String id2, String id3, String rssi, String manufacturerId, String manufacturerReserved) {
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

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        advertiser.startAdvertising(settings, createAdvertiseData(id1, id2, id3, rssi, manufacturerId,manufacturerReserved), advertiseCallback);
    }

    private AdvertiseData createAdvertiseData(String id1, String id2, String id3, String rssi, String manufacturerId, String manufacturerReserved) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

        // ALT 格式的 manufacturer data
        byte[] manufacturerData = new byte[24];
        manufacturerData[0] = (byte) 0xBE;
        manufacturerData[1] = (byte) 0xAC;

        byte[] id1Bytes = hexStringToByteArray(id1);
        byte[] id2Bytes = hexStringToByteArray(id2);
        byte[] id3Bytes = hexStringToByteArray(id3);

        System.arraycopy(id1Bytes, 0, manufacturerData, 2, 16);
        System.arraycopy(id2Bytes, 0, manufacturerData, 18, 2);
        System.arraycopy(id3Bytes, 0, manufacturerData, 20, 2);
        manufacturerData[22] = Byte.parseByte(rssi);

        byte[] manufacturerReservedBytes = hexStringToByteArray(manufacturerReserved);
        System.arraycopy(manufacturerReservedBytes, 0, manufacturerData, 23, 1);


        Log.e("TAG", "manufacturerData => " + bytesToHex(manufacturerData));
        dataBuilder.addManufacturerData(0xBCDE, manufacturerData);

        return dataBuilder.build();
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // 广播开始
            Log.d("AltBeacon", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // 广播失败
            Log.e("AltBeacon", "Advertising start failed: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback);
    }

    // 辅助方法：将十六进制字符串转换为字节数组
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // 辅助方法：将字节数组转换为十六进制字符串
    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
