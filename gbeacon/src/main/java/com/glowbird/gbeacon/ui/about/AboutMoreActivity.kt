package com.glowbird.gbeacon.ui.about

import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityAboutMoreBinding
import com.glowbird.gbeacon.ui.base.BaseActivity

/**
 * 关于更多活动页面。
 */
class AboutMoreActivity : BaseActivity<ActivityAboutMoreBinding>() {

    override fun getViewBinding(): ActivityAboutMoreBinding {
        return ActivityAboutMoreBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupUI()
    }

    override fun initEvent() {
        setupEvents()
    }

    /**
     * 设置UI组件。
     */
    private fun setupUI() {
        with(binding) {
            // 设置标题和返回按钮
            header.headerTitle.text = getString(R.string.about_title)
            header.headerLeft.text = getString(R.string.back)
            // 设置关于信息文本
            aboutText.text = getString(R.string.about_1)
        }
    }

    /**
     * 设置事件监听器。
     */
    private fun setupEvents() {
        binding.header.headerLeft.setOnClickListener {
            finish()  // 关闭活动
        }
    }
}
