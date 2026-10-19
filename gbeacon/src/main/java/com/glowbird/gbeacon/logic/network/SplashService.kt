package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.SplashInfo
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * SplashService 接口定义了用于获取启动屏幕信息和下载启动屏幕图像的 API
 */

interface SplashService {

    /**
     * 获取启动屏幕信息
     * @param parameter 参数的映射表
     * @return SplashInfo 含有启动屏幕信息的对象
     */
    @POST("lanch")
    suspend fun getSplash(@Body parameter: Map<String, String>): SplashInfo

    /**
     * 下载启动屏幕图像
     * 使用 @Streaming 注解以便在处理大文件时避免内存溢出（OOM）
     * @param path 路径，默认为 "beacon"
     * @param fileName 文件名，默认为 "lanch.png"
     * @return ResponseBody 包含图像的响应体
     */
    @Streaming // 处理大文件时使用，避免内存溢出
    @GET("https://image.feasycom.com/lanchImage/{parameter}/{fileName}")
    suspend fun downloadImage(
        @Path("parameter") path: String = "beacon",
        @Path("fileName") fileName: String = "lanch.png"
    ): ResponseBody

}