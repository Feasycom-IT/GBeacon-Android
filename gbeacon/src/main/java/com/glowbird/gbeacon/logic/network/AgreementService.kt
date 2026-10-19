package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.ProtocolParams
import com.glowbird.gbeacon.logic.model.ProtocolResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 定义与协议服务相关的网络请求接口。
 */

interface AgreementService {

    /**
     * 使用 POST 请求获取协议响应
     * @param protocolParams 请求体中的协议参数
     * @param local 请求头中的区域设置，默认为 "en"
     * @return 协议响应对象 [ProtocolResponse]
     */
    @POST("agreement")
    suspend fun getProtocol(
        @Body protocolParams: ProtocolParams,
        @Header("local") local: String = "en"
    ): ProtocolResponse

}