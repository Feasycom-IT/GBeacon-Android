package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.BeaconParameter
import com.glowbird.gbeacon.logic.model.Devices
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * BeaconService 接口用于定义与 Beacon 相关的网络请求
 */

interface BeaconService {

    /**
     * 使用提供的 Beacon 参数查询所有设备信息
     * @param beaconParameter 包含查询参数的对象
     * @return 查询结果的 Devices 对象，可能为 null
     */
    @POST("allDevice")
    suspend fun getAllBeacon(@Body beaconParameter: BeaconParameter): Devices?

}
