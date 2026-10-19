package com.glowbird.gbeacon.logic.model

/**
 * 表示蓝牙 Beacon 设备的参数。
 * @property deviceType 设备类型。
 * @property funcType 功能类型。
 * @property hash 哈希值，可能用于唯一标识。
 * @property haveDetail 标志位，表示是否有详细信息。
 * @property page 分页信息，表示当前页面编号。
 * @property size 分页信息，表示每页的大小。
 */

data class BeaconParameter(
    val deviceType: Int,
    val funcType: Int,
    val hash: String?,
    val haveDetail: Boolean,
    val page: Int,
    val size: Int
)