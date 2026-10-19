package com.glowbird.gbeacon.logic.model

/**
 * 表示订单数据的数据类。
 * @property time 订单时间。
 * @property name 订单名称，可以为空。
 * @property address 订单地址，可以为空。
 * @property connect 连接状态。
 * @property map 订单相关的映射，键值对表示订单状态，可以为空。
 */

data class Order(
    val time: String,
    val name: String?,
    val address: String?,
    val connect: Boolean,
    val map: LinkedHashMap<String, Boolean>?
)