package com.glowbird.gbeacon.logic.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * DfuService 接口定义了用于下载 DFU 文件的网络请求
 */

interface DfuService {

    /**
     * 使用给定的文件名下载 DFU 文件
     * @param name 文件名（不含文件扩展名）
     * @return 下载的 DFU 文件作为 ResponseBody 对象返回
     */
    @Streaming
    @GET("https://dfu.feasycom.com/{file_name}.dfu")
    suspend fun downloadDFU(@Path("file_name") name: String): ResponseBody

}
