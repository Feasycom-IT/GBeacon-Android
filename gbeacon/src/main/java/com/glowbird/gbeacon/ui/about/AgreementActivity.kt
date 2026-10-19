package com.glowbird.gbeacon.ui.about

import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityAgreementBinding
import com.glowbird.gbeacon.logic.model.ProtocolParams
import com.glowbird.gbeacon.logic.network.BeaconNetwork
import com.glowbird.gbeacon.ui.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 协议页面，用于展示用户协议或隐私政策
 */

class
AgreementActivity : BaseActivity<ActivityAgreementBinding>() {

    // 获取活动的类型（协议类型）
    private val agreementType by lazy {
        intent.getIntExtra("type", 0)
    }

  /*  private val USER_AGREEMENT_CN =
        "https://document.feasycom.com/file/agreement/beacon/userAgreement.html"
    private val USER_AGREEMENT_EN =
        "https://document.feasycom.com/file/agreement/beacon/userAgreement_en.html"
    private val PRIVACY_AGREEMENT_CN =
        "https://document.feasycom.com/file/agreement/beacon/privacyAgreement.html"
    private val PRIVACY_AGREEMENT_EN =
        "https://document.feasycom.com/file/agreement/beacon/privacyAgreement_en.html"*/


    private val USER_AGREEMENT_CN =
       "file:///android_asset/userAgreement.html"
    private val USER_AGREEMENT_EN =
        "file:///android_asset/userAgreement_en.html"
    private val PRIVACY_AGREEMENT_CN =
        "file:///android_asset/privacyAgreement.html"
    private val PRIVACY_AGREEMENT_EN =
       "file:///android_asset/privacyAgreement_en.html"

    override fun getViewBinding() = ActivityAgreementBinding.inflate(layoutInflater)

    /**
     * 初始化视图
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView() {
        // 请求协议数据
        requestAgreement(agreementType)
        // 根据类型设置标题
        binding.header.headerTitle.text = when (agreementType) {
            1 -> getString(R.string.userAgreement)
            else -> getString(R.string.privacyAgreement)
        }
        // 设置返回按钮文本
        binding.header.headerLeft.text = getString(R.string.back)
    }

    /**
     * 初始化事件
     */
    override fun initEvent() {
        // 设置返回按钮点击事件
        binding.header.headerLeft.setOnClickListener {
            finish()
        }
    }

    /**
     * 根据协议类型请求协议数据。
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun requestAgreement(type: Int) {
        if (type == 1) {
            // 用户协议
            val localeLanguage = try {
                if (resources.configuration.locales[0].language.endsWith("zh")) "cn" else "en"
            } catch (e: NoSuchMethodError) {
                if (resources.configuration.locale.language.endsWith("zh")) "cn" else "en"
            }
            if (localeLanguage == "cn") {
                configureWebView(USER_AGREEMENT_CN)
            } else {
                configureWebView(USER_AGREEMENT_EN)
            }
        } else {
            // 隐私协议
            val localeLanguage = try {
                if (resources.configuration.locales[0].language.endsWith("zh")) "cn" else "en"
            } catch (e: NoSuchMethodError) {
                if (resources.configuration.locale.language.endsWith("zh")) "cn" else "en"
            }
            if (localeLanguage == "cn") {
                configureWebView(PRIVACY_AGREEMENT_CN)
            } else {
                configureWebView(PRIVACY_AGREEMENT_EN)
            }
        }
//        // 构建协议参数
//        val protocolParams = ProtocolParams("beacon", type)
//        // 使用协程进行网络请求
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                // 获取当前语言环境
//                val localeLanguage = try {
//                    if (resources.configuration.locales[0].language.endsWith("zh")) "cn" else "en"
//                } catch (e: NoSuchMethodError) {
//                    if (resources.configuration.locale.language.endsWith("zh")) "cn" else "en"
//                }
//                // 请求协议数据
//                val protocolResponse = BeaconNetwork.getProtocol(protocolParams, localeLanguage)
//                // 判断请求是否成功
//                if (protocolResponse.code == 200) {
//                    // 请求成功，在主线程更新UI
//                    launch(Dispatchers.Main) {
//                        configureWebView(protocolResponse.data.url)
//                    }
//                } else {
//                    // 请求失败，显示错误对话框
//                    showErrorDialog()
//                }
//            } catch (e: Exception) {
//                // 捕获异常，显示错误对话框
//                launch(Dispatchers.Main) {
//                    showErrorDialog()
//                }
//            }
//        }
    }

    /**
     * 配置WebView并加载协议网页。
     */
    private fun configureWebView(url: String) {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // 更新进度条的进度
                binding.progressBar.progress = newProgress
                // 根据进度条状态显示或隐藏进度条
                binding.progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }
        // 加载网页
        binding.webView.loadUrl(url)
    }

    /**
     * 显示网络错误对话框。
     */
    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setMessage(getString(R.string.network_error))
            .setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }

}
