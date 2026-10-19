package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.FeedbackParams
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * FeedbackService 接口定义了用于发送反馈的网络请求
 */

interface FeedbackService {

    /**
     * 发送反馈信息
     * @param key 用于验证请求的密钥
     * @param content 包含反馈内容的 FeedbackParams 对象
     * @return 服务器的响应作为 ResponseBody 对象返回
     */
    @POST("https://qyapi.weixin.qq.com/cgi-bin/webhook/send")
    suspend fun feedback(@Query("key") key: String, @Body content: FeedbackParams): ResponseBody

}