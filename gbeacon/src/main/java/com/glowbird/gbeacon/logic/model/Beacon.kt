package com.glowbird.gbeacon.logic.model

import com.feasycn.bean.BluetoothDeviceWrapper
import com.glowbird.gbeacon.logic.utils.ShowBroadcastStatus
import kotlin.math.min

/**
 * 表示蓝牙 Beacon 设备的模型类。
 * @property device BluetoothDeviceWrapper 对象，表示设备信息。
 * @property lastTimestamp 上次接收到信号的时间戳。
 */

class Beacon(var device: BluetoothDeviceWrapper, var lastTimestamp: Long = 0) {

    var intervalNanos: Long = 0
    var count: Int = 0
    var foldStatus: ShowBroadcastStatus = ShowBroadcastStatus.NONE

    /**
     * 更新信号间隔。
     * @param interval 信号间隔时间（纳秒）。
     */
    fun updateInterval(interval: Long) {
        if (interval <= 0) return

        intervalNanos = if (intervalNanos == 0L) {
            interval
        } else {
            calculateNewInterval(interval)
        }
    }

    /**
     * 计算新的信号间隔时间。
     * @param interval 当前信号间隔时间（纳秒）。
     * @return 计算后的信号间隔时间。
     */
    private fun calculateNewInterval(interval: Long): Long {
        return when {
            interval < intervalNanos * 0.7 && count < 10 -> interval
            interval < intervalNanos + 3_000_000 -> {
                val limitedCount = min(count, 10)
                (intervalNanos * (limitedCount - 1) + interval) / limitedCount
            }
            interval < intervalNanos * 1.4 -> (intervalNanos * 29 + interval) / 30
            else -> intervalNanos
        }
    }

    /**
     * 判断当前 Beacon 对象是否与另一个对象相等。
     * @param other 另一个对象。
     * @return 如果相等，则返回 true，否则返回 false。
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Beacon -> other.device.address == device.address
            is BluetoothDeviceWrapper -> other.address == device.address
            else -> false
        }
    }

    /**
     * 返回当前 Beacon 对象的哈希值。
     * @return 当前 Beacon 对象的哈希值。
     */
    override fun hashCode(): Int {
        return device.hashCode()
    }
}
