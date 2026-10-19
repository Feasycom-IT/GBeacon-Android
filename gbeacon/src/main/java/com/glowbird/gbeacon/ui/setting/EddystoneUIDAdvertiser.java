package com.glowbird.gbeacon.ui.setting;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EddystoneUIDAdvertiser {

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseData advertiseData;

    @SuppressLint("MissingPermission")
    public void startAdvertising(Context context, String namespace, String instance, String reserved, String rssi) {
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

        byte[] uidFrame = createUIDFrame(namespace, instance, reserved, rssi);

        advertiseData = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(UUID.fromString("0000feaa-0000-1000-8000-00805f9b34fb")), uidFrame)
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .build();

        advertiser.startAdvertising(settings, advertiseData, advertiseCallback);
    }

    private byte[] createUIDFrame(String namespace, String instance, String reserved, String rssi) {
        ByteBuffer buffer = ByteBuffer.allocate(20);

        // Frame Type
        buffer.put((byte) 0x00);

        // Ranging Data
        buffer.put(Byte.parseByte(rssi));

        // Namespace ID
        buffer.put(hexStringToByteArray(namespace));

        // Instance ID
        buffer.put(hexStringToByteArray(instance));

        // Reserved
        buffer.put(hexStringToByteArray(reserved));

        return buffer.array();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // Advertising started successfully
            Log.d("Eddystone-UID", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // Advertising failed to start
            Log.e("Eddystone-UID", "Advertising start failed: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback);
    }
}
