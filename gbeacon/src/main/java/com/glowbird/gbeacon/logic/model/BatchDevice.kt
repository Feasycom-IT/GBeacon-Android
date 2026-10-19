package com.glowbird.gbeacon.logic.model

import com.feasycn.bean.BluetoothDeviceWrapper

/**
 * 表示一批设备的模型类。
 * @property device BluetoothDeviceWrapper 对象，表示设备信息。
 * @property isClick 标志位，表示设备是否被点击。默认为 false。
 */

data class BatchDevice(var device: BluetoothDeviceWrapper, var isClick: Boolean = false) {

    /**
     * 检查当前 BatchDevice 对象是否与另一个对象相等。
     * @param other 另一个对象。
     * @return 如果两个对象相等，则返回 true，否则返回 false。
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BatchDevice) return false

        if (device != other.device) return false

        return true
    }

    /**
     * 返回当前 BatchDevice 对象的哈希值。
     * @return 当前 BatchDevice 对象的哈希值。
     */
    override fun hashCode(): Int {
        return device.hashCode()
    }

}