package com.glowbird.gbeacon.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * 表示设备集合的数据类
 * @property code 响应代码
 * @property data 设备数据，包括哈希、设备列表、页码和总数
 * @property msg 响应消息
 */

data class Devices(
    val code: Int, val `data`: Data, val msg: String
) {
    /**
     * 包含设备详细信息的内部数据类
     * @property hash 数据哈希
     * @property list 设备列表
     * @property page 当前页码
     * @property total 总记录数
     */
    data class Data(
        val hash: String, val list: List<DeviceInfo>, val page: Int, val total: Int
    )
}

/**
 * 表示设备信息的实体类
 * 标记为 Room 数据库的实体
 * @property createTime 设备创建时间
 * @property defaultInterval 默认间隔时间
 * @property deviceId 设备ID
 * @property deviceType 设备类型
 * @property funcType 功能类型
 * @property gsensorDuration 传感器持续时间
 * @property gsensorInterval 传感器间隔时间
 * @property gsensorSensitivity 传感器灵敏度
 * @property keyDuration 按键持续时间
 * @property keyInterval 按键间隔时间
 * @property modifyTime 设备修改时间
 * @property name 设备名称
 * @property number 设备编号
 * @property txPower 发射功率
 */
@Entity
data class DeviceInfo(
    val createTime: Int,
    val defaultInterval: String,
    val deviceId: String? = null,
    val deviceType: Int,
    val funcType: Int,
    val gsensorDuration: String,
    val gsensorInterval: String,
    val gsensorSensitivity: String,
    val keyDuration: String,
    val keyInterval: String,
    val modifyTime: Int,
    val name: String,
    val number: Int,
    val txPower: String
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

data class DeviceData(
    val createTime: Long,
    val defaultInterval: String,
    val deviceId: String,
    val deviceType: Int,
    val funcType: Int,
    val gsensorDuration: String,
    val gsensorInterval: String,
    val gsensorSensitivity: String,
    val id: Int,
    val keyDuration: String,
    val keyInterval: String,
    val modifyTime: Long,
    val name: String,
    val number: Int,
    val txPower: String
)

data class DeviceDataWrapper(
    val hash: String,
    val list: List<DeviceData>,
    val page: Int,
    val size: Int,
)

data class DeviceJson(
    val code: Int,
    val data: DeviceDataWrapper,
    val msg: String
)