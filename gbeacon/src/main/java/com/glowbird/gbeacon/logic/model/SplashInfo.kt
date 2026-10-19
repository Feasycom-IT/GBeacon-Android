package com.glowbird.gbeacon.logic.model

/**
 * 表示启动画面信息的数据类
 * @property code 响应代码
 * @property data 启动画面数据
 * @property msg 响应消息
 */

data class SplashInfo(
    val code: Int,
    val `data`: Data,
    val msg: String
) {
    /**
     * 表示启动画面数据的数据类
     * @property image 启动画面图像的 URL
     * @property version 启动画面版本
     */
    data class Data(
        val image: String,
        val version: Int
    )
}

