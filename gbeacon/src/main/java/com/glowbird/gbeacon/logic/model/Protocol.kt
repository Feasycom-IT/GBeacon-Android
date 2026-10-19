package com.glowbird.gbeacon.logic.model

import androidx.annotation.Keep

/**
 * 表示协议参数的数据类
 * @property app 应用程序名称
 * @property type 协议类型
 */
@Keep // 确保代码在混淆时保持不变
data class ProtocolParams(val app: String, val type: Int)

/**
 * 表示协议响应的数据类
 * @property code 响应代码
 * @property data 响应数据
 * @property msg 响应消息
 */
@Keep
data class ProtocolResponse(
    val code: Int, val `data`: Data, val msg: String
)

/**
 * 表示响应数据的数据类
 * @property url 响应数据的 URL
 */
@Keep
data class Data(
    val url: String
)