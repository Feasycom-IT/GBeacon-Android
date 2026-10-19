package com.glowbird.gbeacon.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *　ServiceCreator 对象负责创建 Retrofit 服务实例
 */

object ServiceCreator {
    // 基础 URL, 用于与 glowbird API 通信
    private const val BASE_URL = "https://api.feasycom.com/"

    // 创建 Retrofit 实例
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
        .build()

    /**
     * 创建指定服务类的 Retrofit 实例
     * @param T 服务类的类型
     * @param serviceClass 服务类的 Class 对象
     * @return 指定服务类的 Retrofit 实例
     */
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    /**
     * 创建指定服务类的 Retrofit实例
     * 使用 inline 函数和 reified 类型参数避免了传递 Class 对象
     * @param T 服务类的类型
     * @return 指定服务类的 Retrofit 实例
     */
    inline fun <reified T> create(): T = create(T::class.java)

}