package com.glowbird.gbeacon.ui.forum

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentForumBinding
import com.glowbird.gbeacon.ui.base.BaseFragment

class ForumFragment : BaseFragment<FragmentForumBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentForumBinding.inflate(inflater, container, false)

    override fun initView() {
        binding?.apply {
            foot.navigationBar.check(R.id.forum_nav)
            header.headerTitle.text = getString(R.string.forum)
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
                    LogUtils.d(TAG, "initView onReceivedSslError ")
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
                        if (progressBar.visibility == View.GONE) {
                            progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        binding?.webView?.loadUrl("https://forum.feasycom.com/")
    }

    override fun onResume() {
        super.onResume()
        binding?.foot?.navigationBar?.check(R.id.forum_nav)
    }

    override fun onDestroyView() {
        // 安全清理 WebView
        binding?.webView?.apply {
            stopLoading()
            webChromeClient = null
          //  webViewClient = null
        }
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "ForumFragment"
    }
}