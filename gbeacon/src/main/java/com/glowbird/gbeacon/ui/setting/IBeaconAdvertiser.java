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

import java.nio.ByteBuffer;
import java.util.UUID;

public class IBeaconAdvertiser {

    private BluetoothLeAdvertiser advertiser;

    // 启动广播
    public void start(Context context, String customUuid, String customMajor, String customMinor, String customRssi) {
        UUID uuid = UUID.fromString(customUuid);
        String major = customMajor;
        String minor = customMinor;
        String txPower = customRssi;
        startAdvertising(context, uuid, major, minor, txPower);
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising(Context context, UUID uuid, String major, String minor, String txPower) {
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

        advertiser.startAdvertising(settings, createAdvertiseData(uuid, major, minor, txPower), advertiseCallback);
    }

    private AdvertiseData createAdvertiseData(UUID uuid, String major, String minor, String txPower) {
        Log.e("TAG","major => " + major + "  minor => " + minor + "  txPower => " + txPower);
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

        // iBeacon 格式的 manufacturer data
        byte[] manufacturerData = new byte[23];
        manufacturerData[0] = 0x02; // 0x02 是 iBeacon 标志
        manufacturerData[1] = 0x15; // iBeacon 负载长度

        // 设置 UUID（16字节）
        System.arraycopy(uuidToBytes(uuid), 0, manufacturerData, 2, 16);
        // 设置 Major 值（2字节）
        manufacturerData[18] = (byte) (Integer.valueOf(major) >> 8);
        manufacturerData[19] = (byte) (Integer.valueOf(major) & 0xFF);
        // 设置 Minor 值（2字节）
        manufacturerData[20] = (byte) (Integer.valueOf(minor) >> 8);
        manufacturerData[21] = (byte) (Integer.valueOf(minor) & 0xFF);
        // 设置 TX 功率（1字节）
        manufacturerData[22] = Byte.parseByte(txPower);

        dataBuilder.addManufacturerData(0x004C, manufacturerData); // 0x004C 是苹果公司（iBeacon）的标识符
        return dataBuilder.build();
    }

    // 将 UUID 转换为字节数组
    private byte[] uuidToBytes(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(uuidBytes);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return uuidBytes;
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // 广播开始
            Log.d("iBeacon", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // 广播失败
            Log.e("iBeacon", "Advertising start failed: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        if (advertiser != null) {
            advertiser.stopAdvertising(advertiseCallback);
        }
    }

}