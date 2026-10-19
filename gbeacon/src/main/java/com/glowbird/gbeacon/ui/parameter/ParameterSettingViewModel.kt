package com.glowbird.gbeacon.ui.parameter

import androidx.lifecycle.ViewModel
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.bean.FeasyBeacon
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback

/**
 * 参数设置的ViewModel，用于处理与参数设置相关的逻辑
 */
class ParameterSettingViewModel : ViewModel() {

    /**
     * 检查是否连接到设备
     */
    fun isConnect() = BluetoothRepository.isConnect()

    /**
     * 连接到指定设备
     * @param device 蓝牙设备对象
     * @param pin 连接PIN码
     */
    fun connect(device: BluetoothDeviceWrapper, pin: String) =
        BluetoothRepository.connect(device, pin)

    /**
     * 注册BLE回调
     * @param callback BLE回调接口
     */
    fun registerViewCallback(callback: FscBleCallback) =
        BluetoothRepository.registerViewCallback(callback)

    /**
     * 取消注册BLE回调
     * @param callback BLE回调接口
     */
    fun unRegisterViewCallback(callback: FscBleCallback) =
        BluetoothRepository.unRegisterViewCallback(callback)

    /**
     * 获取设备信息
     * @param feasyBeacon FeasyBeacon对象
     * @param isTxPower 是否需要获取TX Power信息
     */
    fun getDeviceInfo(feasyBeacon: FeasyBeacon, isTxPower: Boolean) = BluetoothRepository.getDeviceInfo(feasyBeacon, isTxPower)

    /**
     * 检查Beacon信息是否完整
     */
    fun isBeaconInfoFull() = BluetoothRepository.isBeaconInfoFull()

}
