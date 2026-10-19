package com.glowbird.gbeacon.ui.ad

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.glowbird.gbeacon.databinding.FragmentAdBinding
import com.glowbird.gbeacon.logic.utils.getStr
import com.glowbird.gbeacon.ui.MainActivity
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.base.BaseActivity2

class AdActivity : BaseActivity2<FragmentAdBinding>() {

    private lateinit var countDownTimer: CountDownTimer

    // 获取 ViewBinding 对象的方法
    override fun getViewBinding() = FragmentAdBinding.inflate(layoutInflater)

    override fun initView() {

        loadCachedAdData()
    }



    override fun onResume() {
        super.onResume()
        initCountDownTimer()
    }
    private var remainingTime: Long = 5000 // 总时长6秒
    private fun initCountDownTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                binding.toMain.text = "${millisUntilFinished / 1000} s"
            }

            override fun onFinish() {
                navigateToBeaconActivity()
            }
        }
        gg = true
        countDownTimer.start()


        binding.toMain.setOnClickListener {

            navigateToBeaconActivity()
        }
    }

    private fun isChineseLanguage(): Boolean {
        return resources.configuration.locales[0].language.endsWith("zh")
    }

    private fun loadCachedAdData() {
        val isChinese = isChineseLanguage()
        val imageKey = if (isChinese) "lanch1" else "lanch2"
        val urlKeys = if (isChinese) listOf("cnurl1", "cnurl2", "cnurl3")
        else listOf("enurl1", "enurl2", "enurl3")

        val cachedImagePath = getStr(imageKey)
        val cachedUrls = urlKeys.map { getStr(it) }

        if (TextUtils.isEmpty(cachedImagePath)) {
            countDownTimer.cancel()
            navigateToBeaconActivity()
        } else {
            LogUtils.d(TAG, "APP AD load cached image => $cachedImagePath")
            Glide.with(this)
                .load(cachedImagePath)
                .into(binding.adImg)

            cachedUrls.forEachIndexed { index, url ->
                if (!TextUtils.isEmpty(url)) {
                    val button = when (index) {
                        0 -> binding.button1Area
                        1 -> binding.button2Area
                        else -> binding.button3Area
                    }
                    setupAdButton(button, url!!)
                }
            }
        }
    }

    private fun setupAdButton(buttonArea: View, url: String) {
        buttonArea.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    countDownTimer.cancel()
                    startActivity(intent)
                } else {
                    ToastUtils.showShort("未找到浏览器应用")
                }
            } catch (e: Exception) {
                ToastUtils.showShort("打开链接失败: ${e.message}")
            } finally {
                countDownTimer.cancel()
            }
        }
    }

    private var gg: Boolean = true // 总时长6秒
    /**
     * 跳转到 Beacon 页面
     */
    private fun navigateToBeaconActivity() {

            startActivity(Intent(this, MainActivity::class.java))
            finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    companion object {
        const val TAG = "AdActivity"
    }
}