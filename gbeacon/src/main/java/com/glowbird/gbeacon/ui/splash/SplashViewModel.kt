package com.glowbird.gbeacon.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.glowbird.gbeacon.logic.BeaconRepository

/**
 * SplashViewModel用于处理启动页相关的业务逻辑
 */
class SplashViewModel : ViewModel() {
    private val _splashLiveData = MutableLiveData<String>()

    /**
     * 将_splashLiveData转换为mSplashLiveData，以便根据数据变化更新UI
     */
    val mSplashLiveData = _splashLiveData.switchMap {
        BeaconRepository.downloadSplash(it)
    }

    /**
     * 下载启动页图片
     * @param app 应用名称
     */
    fun downloadSplash(app: String) {
        _splashLiveData.value = app
    }
}
