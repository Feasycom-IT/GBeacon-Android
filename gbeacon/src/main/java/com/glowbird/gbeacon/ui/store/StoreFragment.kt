package com.glowbird.gbeacon.ui.store

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.navigation.fragment.findNavController
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentStoreBinding
import com.glowbird.gbeacon.ui.base.BaseFragment

/**
 * 应用商店页面
 */
class StoreFragment : BaseFragment<FragmentStoreBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentStoreBinding.inflate(inflater, container, false)

    override fun initView() {
        binding?.apply {
            foot.navigationBar.check(R.id.store_nav)
            header.headerTitle.text = getString(R.string.store)
        }

        binding?.webView?.settings?.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
            databaseEnabled = true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initEvent() {
        binding?.apply {
            foot.beaconNav.setOnClickListener {
                findNavController().navigate(R.id.beaconFragment)
            }
            foot.sensorNav.setOnClickListener {
                findNavController().navigate(R.id.sensorFragment)
            }
            foot.settingNav.setOnClickListener {
                findNavController().navigate(R.id.settingFragment)
            }
            foot.aboutNav.setOnClickListener {
                findNavController().navigate(R.id.about_nav)
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView?, handler: SslErrorHandler?, error: SslError?
                ) {
                    super.onReceivedSslError(view, handler, error)
                    Log.e(TAG, "onReceivedSslError: ")
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }
            }

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.progress = newProgress
                    if (newProgress == 100) {
                        progressBar.visibility = View.GONE
                    } else {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding?.webView?.loadUrl("https://www.feasycom.com.cn/products/")
    }

    override fun onResume() {
        super.onResume()
        binding?.foot?.navigationBar?.check(R.id.store_nav)
    }

    override fun onDestroyView() {
        // 安全销毁 WebView，防止内存泄漏 & 空指针
        binding?.webView?.apply {
            stopLoading()
        //    webViewClient = null
            webChromeClient = null
        }
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "StoreFragment"
    }
}