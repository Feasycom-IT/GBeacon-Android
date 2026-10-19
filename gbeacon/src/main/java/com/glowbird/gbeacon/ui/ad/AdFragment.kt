package com.glowbird.gbeacon.ui.ad

import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentAdBinding
import com.glowbird.gbeacon.logic.utils.getStr
import com.glowbird.gbeacon.ui.base.BaseFragment

class AdFragment : BaseFragment<FragmentAdBinding>() {

    // 获取 ViewBinding 对象的方法
    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentAdBinding.inflate(inflater, container, false)

    override fun initView() {
        loadCachedAdData()
    }

    override fun initEvent() {
        setupClickListeners()
    }

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding?.toMain?.text = "跳过 ${millisUntilFinished / 1000} s"
            }

            override fun onFinish() {
                findNavController().navigate(R.id.action_adFragment_to_beaconFragment)
            }
        }
    }

    private fun setupClickListeners() {
        binding?.toMain?.setOnClickListener {
            countDownTimer.cancel()
            findNavController().navigate(R.id.action_adFragment_to_beaconFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        countDownTimer.start()
    }

    private fun isChineseLanguage(): Boolean {
        return resources.configuration.locales[0].language.endsWith("zh")
    }

    private fun loadCachedAdData() {
        val b = binding ?: return
        val isChinese = isChineseLanguage()
        val imageKey = if (isChinese) "lanch1" else "lanch2"
        val urlKeys = if (isChinese) listOf("cnurl1", "cnurl2", "cnurl3")
        else listOf("enurl1", "enurl2", "enurl3")

        val cachedImagePath = requireContext().getStr(imageKey)
        val cachedUrls = urlKeys.map { requireContext().getStr(it) }

        if (TextUtils.isEmpty(cachedImagePath)) {
            countDownTimer.cancel()
            findNavController().navigate(R.id.action_adFragment_to_beaconFragment)
        } else {
            LogUtils.d(TAG, "APP AD load cached image => $cachedImagePath")
            Glide.with(requireActivity())
                .load(cachedImagePath)
                .into(b.adImg)

            cachedUrls.forEachIndexed { index, url ->
                if (!TextUtils.isEmpty(url)) {
                    val button = when (index) {
                        0 -> b.button1Area
                        1 -> b.button2Area
                        else -> b.button3Area
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
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer.cancel()
    }

    companion object {
        const val TAG = "AdFragment"
    }

}