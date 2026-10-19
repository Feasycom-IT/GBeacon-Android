package com.glowbird.gbeacon.logic.interfaces

import com.feasycn.bean.BeaconBean
import com.feasycn.bean.BluetoothDeviceWrapper

/**
 * 接口定义了蓝牙设备相关的回调方法。
 */

interface FscBleCallback {

    /**
     * 当发现 Beacon 设备时的回调。
     * @param device 包含蓝牙设备信息的 BluetoothDeviceWrapper 对象。
     */
    fun onBeacon(device: BluetoothDeviceWrapper){}

    /**
     * 当发现传感器设备时的回调。
     * @param device 包含蓝牙设备信息的 BluetoothDeviceWrapper 对象。
     */
    fun onSensor(device: BluetoothDeviceWrapper){}

    /**
     * 当设置设备时的回调。
     * @param device 包含蓝牙设备信息的 BluetoothDeviceWrapper 对象。
     */
    fun onSetting(device: BluetoothDeviceWrapper){}

    /**
     * 当连接成功时的回调。
     */
    fun onConnectedSuccess(){}

    /**
     * 连接进度更新的回调。
     * @param status 当前连接进度的状态。
     */
    fun onConnectProgressUpdate(status: Int){}

    /**
     * 当接收到设备信息时的回调。
     * @param parameterName 参数名称。
     * @param parameter 参数值。
     */
    fun onDeviceInfo(parameterName: String?, parameter: Any?){}

    /**
     * 当接收到 AT 命令回调时。
     * @param command 接收到的命令。
     * @param param 命令的参数。
     * @param status 命令的状态。
     */
    fun onAtCommandCallBack(command: String?, param: String?, status: String?){}

    /**
     * 当接收到数据包时的回调。
     * @param strValue 数据包的字符串表示。
     * @param hexString 数据包的十六进制表示。
     * @param rawValue 数据包的原始字节数组。
     */
    fun onPacketReceived(strValue: String, hexString: String, rawValue: ByteArray){}

    /**
     * 当 OTA 更新进度时的回调。
     * @param percentage OTA 更新的百分比进度。
     */
    fun onOtaProgressUpdate(percentage: Int){}

    /**
     * 当断开连接时的回调。
     */
    fun onDisconnect(){}

    /**
     * 当删除 Beacon 信息时的回调。
     * @param beaconBean 要删除的 BeaconBean 对象。
     */
    fun onDeleteBeaconInfo(beaconBean: BeaconBean){}

}