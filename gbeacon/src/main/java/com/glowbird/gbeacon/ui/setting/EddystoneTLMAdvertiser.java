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

public class EddystoneTLMAdvertiser {

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseData advertiseData;

    @SuppressLint("MissingPermission")
    public void startAdvertising(Context context, String battery, String temperature, String count, String time) {
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

        byte[] uidFrame = createTLMFrame(battery, temperature, count, time);

        advertiseData = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(UUID.fromString("0000feaa-0000-1000-8000-00805f9b34fb")), uidFrame)
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .build();

        advertiser.startAdvertising(settings, advertiseData, advertiseCallback);
    }

    private byte[] createTLMFrame(String battery, String temperature, String count, String time) {
        ByteBuffer buffer = ByteBuffer.allocate(14);

        // Frame Type
        buffer.put((byte) 0x20);

        // 版本
        buffer.put((byte) 0x00);

        // 蓄电池电压
        buffer.put(hexStringToByteArray(battery));

        // 芯片温度
        buffer.put(hexStringToByteArray(temperature));

        // 广播次数
        buffer.put(hexStringToByteArray(count));

        // 运行时间
        buffer.put(hexStringToByteArray(time));

        return buffer.array();
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // Advertising started successfully
            Log.d("Eddystone-TLM", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // Advertising failed to start
            Log.e("Eddystone-TLM", "Advertising start failed: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback);
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

}
