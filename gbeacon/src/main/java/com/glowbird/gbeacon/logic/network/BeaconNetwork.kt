package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.AdParams
import com.glowbird.gbeacon.logic.model.BeaconParameter
import com.glowbird.gbeacon.logic.model.FeedbackParams
import com.glowbird.gbeacon.logic.model.ProtocolParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 管理与信标相关的网络请求
 */

object BeaconNetwork {

    // 创建服务实例
    private val splashService = ServiceCreator.create<SplashService>()
    private val agreementService = ServiceCreator.create<AgreementService>()
    private val beaconService = ServiceCreator.create<BeaconService>()
    private val dfuService = ServiceCreator.create<DfuService>()
    private val feedbackService = ServiceCreator.create<FeedbackService>()
    private val adService = ServiceCreatorAd.create<AdService>()

    /**
     * 获取启动画面数据
     * @param parameter 请求参数
     * @return 启动画面数据
     */
    suspend fun getSplash(parameter: Map<String, String>) = withContext(Dispatchers.IO) {
        splashService.getSplash(parameter)
    }

    /**
     * 获取协议数据
     * @param protocolParams 请求体中的协议参数
     * @param local 请求头中的区域设备，默认为 "en"
     * @return 协议响应
     */
    suspend fun getProtocol(protocolParams: ProtocolParams, local: String = "en") =
        withContext(Dispatchers.IO) {
            agreementService.getProtocol(protocolParams, local)
        }

    /**
     * 获取信息数据
     * @param beaconParameter 请求参数
     * @return 信标数据
     */
    suspend fun getAllBeacon(beaconParameter: BeaconParameter) = withContext(Dispatchers.IO) {
        beaconService.getAllBeacon(beaconParameter)
    }

    /**
     * 下载 DFU 文件
     * @param name 文件名称
     * @return 下载的 DFU 文件
     */
    suspend fun downloadDFU(name: String) = withContext(Dispatchers.IO) {
        dfuService.downloadDFU(name)
    }

    /**
     * 发送反馈
     * @param key 反馈密钥
     * @param content 反馈内容
     * @return 反馈响应
     */
    suspend fun feedback(key: String, content: FeedbackParams) = withContext(Dispatchers.IO) {
        feedbackService.feedback(key, content)
    }

    suspend fun getAdData(adParams: AdParams) =
        adService.getAdData(adParams)

}