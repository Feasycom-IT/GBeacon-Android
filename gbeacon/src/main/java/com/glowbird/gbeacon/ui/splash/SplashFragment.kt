package com.glowbird.gbeacon.ui.splash

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentSplashBinding
import com.glowbird.gbeacon.logic.utils.*
import com.glowbird.gbeacon.ui.ad.AdViewModel
import com.glowbird.gbeacon.ui.base.BaseFragment
import com.glowbird.gbeacon.ui.dialog.FirstDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private lateinit var mFirstDialogFragment: FirstDialogFragment

    // 使用 ViewModel 来管理数据
    private val splashViewModel by viewModels<SplashViewModel>()

    private val adViewModel by viewModels<AdViewModel>()

    // 获取 ViewBinding 对象的方法
    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentSplashBinding.inflate(inflater, container, false)

    // 初始化视图
    override fun initView() {
        // 使用 Glide 加载启动图片
        val b = binding ?: return
        Glide.with(this).load(requireContext().getStr("lanch").ifEmpty { R.drawable.loading }).into(b.lanchImg)
    }

    // 初始化事件监听器
    override fun initEvent() {
        clearFilter()

        splashViewModel.downloadSplash("beacon")

        splashViewModel.mSplashLiveData.observe(this as LifecycleOwner) {
            val imgUrl = it.getOrNull()
            if (imgUrl != null) {
                // 使用协程在后台线程执行
                lifecycleScope.launch {
                    try {
                        val imageFile = withContext(Dispatchers.IO) {
                            val target: FutureTarget<File> =
                                Glide.with(requireActivity()).asFile().load(imgUrl).submit()
                            target.get()
                        }

                        LogUtils.d(TAG, "APP SPLASH downloadImage => ${imageFile.path}")
                        requireContext().putStr("lanch", imageFile.path)

                        // 切回主线程更新UI
                        withContext(Dispatchers.Main) {
                            binding?.lanchImg?.let {
                                Glide.with(requireActivity()).load(imgUrl).into(it)
                            }
                        }
                    } catch (e: Exception) {
                        LogUtils.e(TAG, "Download splash image failed", e)
                    }
                }
            }
        }

        firstIn()
    }

    // 清除过滤器设置
    private fun clearFilter() {
        requireContext().putStr("device_BEACON_name", "")
        requireContext().putStr("device_SENSOR_name", "")
        requireContext().putStr("device_SETTING_name", "")
        requireContext().putStr("device_BATCH_name", "")
        requireContext().putInt("device_BEACON_rssi", -100)
        requireContext().putInt("device_SENSOR_rssi", -100)
        requireContext().putInt("device_SETTING_rssi", -100)
        requireContext().putInt("device_BATCH_rssi", -100)
    }

    private fun firstIn() {
        if (::mFirstDialogFragment.isInitialized) return

        val isFirstTime = requireContext().getBoolean("firstTime", false)
        mFirstDialogFragment = FirstDialogFragment().apply {
            onAgree = {
                requireContext().putBoolean("firstTime", true)
                dismiss()
                findNavController().navigate(R.id.action_splashFragment_to_beaconFragment)
            }
            onRefuse = { requireActivity().finish() }
        }

        if (!isFirstTime && !mFirstDialogFragment.isAdded) {
            // 显示用户协议对话框
            mFirstDialogFragment.show(requireActivity().supportFragmentManager, "firstTime")
        } else {
            preloadAdImage()
        }
    }

    private fun preloadAdImage() {
        LogUtils.d(TAG, "APP SPLASH preloadAdImage...")
        adViewModel.mAdLiveData.observe(this as LifecycleOwner) { result ->
            lifecycleScope.launch {
                try {
                    val adResponse = result.getOrNull()
                    if (adResponse != null) {
                        LogUtils.d(
                            TAG,
                            "APP SPLASH preloadAdImage...11111111 code => ${adResponse.code}"
                        )
                        if (adResponse.code == 0) {
                            LogUtils.d(TAG, "APP SPLASH preloadAdImage...222222222")
                            val isChinese =
                                resources.configuration.locales[0].language.endsWith("zh")
                            val imageUrl =
                                if (isChinese) adResponse.data.cnimg else adResponse.data.enimg
                            val urls = if (isChinese) {
                                listOf(
                                    adResponse.data.cnurl1,
                                    adResponse.data.cnurl2,
                                    adResponse.data.cnurl3
                                )
                            } else {
                                listOf(
                                    adResponse.data.enurl1,
                                    adResponse.data.enurl2,
                                    adResponse.data.enurl3
                                )
                            }

                            // 在后台线程中缓存图片
                            val imageFile = withContext(Dispatchers.IO) {
                                val target = Glide.with(requireActivity())
                                    .asFile()
                                    .load(imageUrl)
                                    .submit()
                                target.get()
                            }

                            val cacheKey = if (isChinese) "lanch1" else "lanch2"
                            requireContext().putStr(cacheKey, imageFile.path)

                            if (isChinese) {
                                requireContext().putStr("cnurl1", urls[0])
                                requireContext().putStr("cnurl2", urls[1])
                                requireContext().putStr("cnurl3", urls[2])
                            } else {
                                requireContext().putStr("enurl1", urls[0])
                                requireContext().putStr("enurl2", urls[1])
                                requireContext().putStr("enurl3", urls[2])
                            }
                            findNavController().navigate(R.id.action_splashFragment_to_adFragment)
                        } else {
                            LogUtils.d(TAG, "APP SPLASH preloadAdImage...3333333")
                            findNavController().navigate(R.id.action_splashFragment_to_adFragment)
                        }
                    } else {
                        LogUtils.d(TAG, "APP SPLASH preloadAdImage...44444444444")
                        findNavController().navigate(R.id.action_splashFragment_to_adFragment)
                    }
                } catch (e: Exception) {
                    LogUtils.e(TAG, "APP SPLASH AdPreload", "预加载广告失败: ${e.message}")
                    findNavController().navigate(R.id.action_splashFragment_to_adFragment)
                }
            }
        }
    }

    companion object {
        const val TAG = "SplashFragment"
        private val AD_URLS = arrayOf(
            "https://www.baidu.com",
            "https://www.feasycom.com",
            "https://www.deepseek.com"
        )
    }

}
