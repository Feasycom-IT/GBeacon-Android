package com.glowbird.gbeacon.logic

import android.os.Build
import androidx.lifecycle.liveData
import com.glowbird.gbeacon.App
import com.glowbird.gbeacon.logic.model.AdParams
import com.glowbird.gbeacon.logic.model.BeaconParameter
import com.glowbird.gbeacon.logic.model.FeedbackParams
import com.glowbird.gbeacon.logic.model.ProtocolParams
import com.glowbird.gbeacon.logic.network.BeaconNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * BeaconRepository 提供与网络相关的操作和数据请求。
 */

object BeaconRepository {

    // 常量定义
    private const val FEASYBEACON_KEY = "903ed4b4-4aff-4aeb-b4e7-d5128eb95714"
    const val FEEDBACK_ADVICE = 0
    const val FEEDBACK_BUG = 1
    const val FEEDBACK_UI = 2
    const val FEEDBACK_COOPERATION = 3

    // 下载启动页图片
    fun downloadSplash(app: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val map = mapOf("app" to app)
            val splashInfo = BeaconNetwork.getSplash(map)
            if (splashInfo.code == 200) {
                Result.success(splashInfo.data.image)
            } else {
                Result.failure(RuntimeException("请求首页图时发生错误 ${splashInfo.code}"))
            }
        }
    }

    // 获取协议URL
    fun getAgreementUrl(type: Int) = fire(Dispatchers.IO) {
        coroutineScope {
            // 判断语言类型
            val language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                App.sContext.resources.configuration.locales[0].language
            } else {
                App.sContext.resources.configuration.locale.language
            }
            val languageCode = if (language.endsWith("zh")) "cn" else "en"

            // 获取协议URL
            val protocolParams = ProtocolParams("beacon", type)
            val protocol = BeaconNetwork.getProtocol(protocolParams, languageCode)
            Result.success(protocol.data.url)
        }
    }

    // 获取所有 Beacon 数据
    fun getAllBeacon(beaconParameter: BeaconParameter) = fire(Dispatchers.IO) {
        coroutineScope {
            val beacon = BeaconNetwork.getAllBeacon(beaconParameter)
            Result.success(beacon)
        }
    }

    fun getAdData() = fire(Dispatchers.IO) {
        coroutineScope {
            val adParams = AdParams("Android", "com.glowbird.gbeacon")
            val adResponse = BeaconNetwork.getAdData(adParams)
            Result.success(adResponse)
        }
    }

    // 反馈
    fun feedback(content: String, feedbackType: Int,email: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val fbType = when (feedbackType) {
                FEEDBACK_ADVICE -> "建议"
                FEEDBACK_BUG -> "功能异常"
                FEEDBACK_UI -> "界面异常"
                FEEDBACK_COOPERATION -> "合作"
                else -> "建议"
            }
            // 获取 APP 和设备信息
            val packageManager = App.sContext.packageManager
            val packageInfo = packageManager.getPackageInfo(App.sContext.packageName, 0)

            val appName = "GBeacon"
            val appVersion = packageInfo.versionName
            val systemVersion = Build.VERSION.RELEASE
            val phoneType = "${Build.BRAND}(${Build.MODEL})"

            // 生成反馈内容
            val feedbackContent = """
                <font color="warning">Android </font>《$appName》用户反馈
                >类型：<font color="comment">$fbType</font>
                >APP版本：<font color="comment">$appVersion</font>
                >系统版本：<font color="comment">$systemVersion</font>
                >手机型号：<font color="comment">$phoneType</font>
                >邮箱：<font color="comment">$email</font>
                >反馈内容：<font color="comment">$content</font>
            """.trimIndent()

            // 提交反馈
            val feedback = BeaconNetwork.feedback(
                FEASYBEACON_KEY, FeedbackParams(FeedbackParams.Markdown(feedbackContent))
            )
            Result.success(feedback)
        }

    }

    // 用于异步操作的辅助函数
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }

}