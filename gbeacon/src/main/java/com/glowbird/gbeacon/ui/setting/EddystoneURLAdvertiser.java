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
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EddystoneURLAdvertiser {

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseData advertiseData;

    @SuppressLint("MissingPermission")
    public void startAdvertising(Context context, String url, String rssi) {
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

        String content = extractContent(url);
        if (content == null) return;
        byte[] byteArray = content.getBytes(StandardCharsets.UTF_8);

        // Convert byte array to hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }

        byte[] urlFrame = createURLFrame(hexString.toString(), rssi);

        Log.e("Eddystone-URL", "uidFrame hexString => " + bytesToHex(urlFrame));

        advertiseData = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(UUID.fromString("0000feaa-0000-1000-8000-00805f9b34fb")), urlFrame)
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .build();

        advertiser.startAdvertising(settings, advertiseData, advertiseCallback);
    }

    private byte[] createURLFrame(String url, String rssi) {
        int bufferSize = 4 + (url.length() / 2);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        // Frame Type
        buffer.put((byte) 0x10);

        // Ranging Data
        buffer.put(Byte.parseByte(rssi));

        // url地址
        buffer.put((byte) 0x03); // 表示 'https://'
        buffer.put(hexStringToByteArray(url));
        buffer.put((byte) 0x07); // 表示 '.com'

        return buffer.array();
    }

    // 启动广播
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            // Advertising started successfully
            Log.d("Eddystone-URL", "Advertising start successful");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            // Advertising failed to start
            Log.e("Eddystone-URL", "Advertising start failed: " + errorCode);
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

    public String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String extractContent(String url) {
        String prefix = "https://";
        int httpsIndex = url.indexOf(prefix);
        if (httpsIndex == -1) return null;

        int startIndex = httpsIndex + prefix.length();
        int comIndex = url.indexOf(".com", startIndex);
        if (comIndex == -1) return null;

        return url.substring(startIndex, comIndex);
    }

}
