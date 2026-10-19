package com.glowbird.gbeacon.ui

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.glowbird.gbeacon.databinding.FragmentSplashBinding
import com.glowbird.gbeacon.logic.utils.*
import com.glowbird.gbeacon.ui.ad.AdActivity
import com.glowbird.gbeacon.ui.ad.AdViewModel
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.base.BaseActivity2
import com.glowbird.gbeacon.ui.dialog.FirstDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : BaseActivity2<FragmentSplashBinding>() {

    private lateinit var mFirstDialogFragment: FirstDialogFragment

    private val adViewModel by viewModels<AdViewModel>()

    // 获取 ViewBinding 对象的方法
    override fun getViewBinding() = FragmentSplashBinding.inflate(layoutInflater)

    // 初始化视图
    override fun initView() {

        clearFilter()
        firstIn()
    }




    // 清除过滤器设置
    private fun clearFilter() {
        putStr("device_BEACON_name", "")
        putStr("device_SENSOR_name", "")
        putStr("device_SETTING_name", "")
        putStr("device_BATCH_name", "")
        putInt("device_BEACON_rssi", -100)
        putInt("device_SENSOR_rssi", -100)
        putInt("device_SETTING_rssi", -100)
        putInt("device_BATCH_rssi", -100)
    }

    private fun firstIn() {
        if (::mFirstDialogFragment.isInitialized) return
        val isFirstTime = getBoolean("firstTime", false)
        mFirstDialogFragment = FirstDialogFragment().apply {
            onAgree = {
                putBoolean("firstTime", true)
                dismiss()
                navigateToBeaconActivity()
            }
            onRefuse = { finish() }
        }

        if (!isFirstTime && !mFirstDialogFragment.isAdded) {
            // 显示用户协议对话框
            mFirstDialogFragment.show(supportFragmentManager, "firstTime")
        } else {
            preloadAdImage()
        }
    }

    /**
     * 检查是否存在缓存的广告图片
     */
    private fun hasCachedAdImage(): Boolean {
        val isChinese = resources.configuration.locales[0].language.endsWith("zh")
        val cacheKey = if (isChinese) "lanch1" else "lanch2"
        val cachedImagePath = getStr(cacheKey, "")

        // 检查缓存路径是否存在且不为空
        return cachedImagePath.isNotEmpty() && try {
            val file = java.io.File(cachedImagePath)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun preloadAdImage() {
        adViewModel.mAdLiveData.observe(this) { result ->
            lifecycleScope.launch {
                try {
                    val adResponse = result.getOrNull()
                    if (adResponse != null) {
                        if (adResponse.code == 0) {
                            // 服务器有数据，缓存图片并跳转到广告页面
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
                                val target = Glide.with(this@SplashActivity)
                                    .asFile()
                                    .load(imageUrl)
                                    .submit()
                                target.get()
                            }

                            val cacheKey = if (isChinese) "lanch1" else "lanch2"
                            putStr(cacheKey, imageFile.path)

                            if (isChinese) {
                                putStr("cnurl1", urls[0])
                                putStr("cnurl2", urls[1])
                                putStr("cnurl3", urls[2])
                            } else {
                                putStr("enurl1", urls[0])
                                putStr("enurl2", urls[1])
                                putStr("enurl3", urls[2])
                            }

                            LogUtils.d(TAG, "广告数据获取成功，缓存图片并跳转到广告页面")
                            navigateToAdActivity()
                        } else {
                            // 服务器返回code不为0（服务器数据被删除），清除缓存并跳转到beacon页面
                            LogUtils.d(TAG, "服务器返回code=${adResponse.code}，清除缓存并跳转到beacon页面")
                            clearAdCache()
                            navigateToBeaconActivity()
                        }
                    } else {
                        // 服务器返回null（服务器数据被删除），清除缓存并跳转到beacon页面
                        LogUtils.d(TAG, "服务器返回null，清除缓存并跳转到beacon页面")
                        clearAdCache()
                        navigateToBeaconActivity()
                    }
                } catch (e: Exception) {
                    LogUtils.e(TAG, "APP SPLASH AdPreload", "预加载广告失败: ${e.message}")
                    // 请求异常，检查是否有缓存图片
                    if (hasCachedAdImage()) {
                        // 有缓存图片，跳转到广告页面
                        LogUtils.d(TAG, "请求异常但存在缓存广告图片，跳转到广告页面")
                        navigateToAdActivity()
                    } else {
                        // 没有缓存图片，跳转到beacon页面
                        LogUtils.d(TAG, "请求异常且无缓存广告图片，跳转到beacon页面")
                        navigateToBeaconActivity()
                    }
                }
            }
        }
    }

    /**
     * 跳转到广告页面
     */
    private fun navigateToAdActivity() {
        startActivity(Intent(this, AdActivity::class.java))
        finish()
    }

    /**
     * 跳转到 Beacon 页面
     */
    private fun navigateToBeaconActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * 清除广告缓存数据
     */
    private fun clearAdCache() {
        try {
            
            putStr("lanch1", "")
            putStr("lanch2", "")

            // 清除URL缓存
            putStr("cnurl1", "")
            putStr("cnurl2", "")
            putStr("cnurl3", "")
            putStr("enurl1", "")
            putStr("enurl2", "")
            putStr("enurl3", "")

            LogUtils.d(TAG, "APP SPLASH clearAdCache: 广告缓存已清除")
        } catch (e: Exception) {
            LogUtils.e(TAG, "APP SPLASH clearAdCache", "清除广告缓存失败: ${e.message}")
        }
    }

    companion object {
        const val TAG = "SplashActivity"
    }
}