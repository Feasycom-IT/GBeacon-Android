package com.glowbird.gbeacon.ui.about

import androidx.activity.viewModels
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycn.util.NetworkUtil
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityFeedbackBinding

import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.dialog.LoadDialogFragment
import com.glowbird.gbeacon.ui.dialog.TipsDialog


/**
 * 创建反馈活动
 */

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    private lateinit var waitDialog: LoadDialogFragment

    // 声明延迟初始化的 viewModel 属性，并通过 viewModels() 扩展函数初始化
    private val viewModel: FeedbackViewModel by viewModels()

    // 获取视图绑定对象的方法
    override fun getViewBinding() = ActivityFeedbackBinding.inflate(layoutInflater)

    // 初始化视图的方法
    override fun initView() {
        // 设置标题和返回按钮的文本
        binding.header.apply {
            headerTitle.text = getString(R.string.feedback)
            headerLeft.text = getString(R.string.feedback_left)
            headerLeft.setOnClickListener { finish() }
        }
    }


    private var mPreferenceKey = "FEEDBACK_ADVICE"


    // 初始化事件的方法
    override fun initEvent() {
        var fbType = "建议"
        binding.proposal.setOnClickListener {
            var fbType = "建议"
            mPreferenceKey = "FEEDBACK_ADVICE"
            binding.abnormalFunction.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.cooperation.setChecked(false)
        }


        binding.abnormalFunction.setOnClickListener {
            fbType = "功能异常"
            mPreferenceKey = "FEEDBACK_BUG"
            binding.proposal.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.cooperation.setChecked(false)
        }
        binding.interfaceAbnormality.setOnClickListener {
            fbType = "界面异常"
            mPreferenceKey = "FEEDBACK_UI"
            binding.abnormalFunction.setChecked(false)
            binding.proposal.setChecked(false)
            binding.cooperation.setChecked(false)
        }
        binding.cooperation.setOnClickListener {
            fbType = "合作"
            mPreferenceKey = "FEEDBACK_COOPERATION"
            binding.abnormalFunction.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.proposal.setChecked(false)
        }



        // 设置提交按钮的点击事件
        binding.submit.setOnClickListener {

            val feedbackText = binding.dataEv.text.toString().trim()
            if (feedbackText.isEmpty()) {
                ToastUtils.showLong(R.string.feedback_data_error)

            } else if (binding.editEmail.text.toString().isEmpty()) {
                ToastUtils.showLong(R.string.feedback_email)

            }else if (!RegexUtils.isEmail(binding.editEmail.text.toString())) {
                ToastUtils.showLong(R.string.feedback_email_erro)
            }else if(feedbackText.isNotEmpty() && binding.editEmail.text.toString().isNotEmpty()){

                // 显示等待对话框
                waitDialog = LoadDialogFragment(getString(R.string.feedback_status))
                waitDialog.show(supportFragmentManager, "feedback")


                NetworkUtil.feedback(
                    feedbackText,
                    fbType,
                    binding.editEmail.text.toString(),
                    this,
                    object : NetworkUtil.FeedbackCallback {
                        override fun onSuccess(response: String?) {
                            // 关闭等待对话框
                            waitDialog.dismiss()
                            runOnUiThread {
                                binding.editEmail.setText("")
                                binding.dataEv.setText("")
                                val tipsDialog = TipsDialog(this@FeedbackActivity)
                                tipsDialog.setInfo(
                                    getString(R.string.feedback_success),
                                    binding.dataEv
                                )
                                tipsDialog.show()
                            }
                        }

                        override fun onFailure(error: String?) {
                            // 关闭等待对话框
                            waitDialog.dismiss()
                            runOnUiThread {
                                val tipsDialog = TipsDialog(this@FeedbackActivity)
                                tipsDialog.setInfo(
                                    getString(R.string.feedback_failure),
                                    binding.dataEv
                                )
                                tipsDialog.show()
                            }
                        }

                        override fun onNetworkError(errorMessage: String?) {
                            // 关闭等待对话框
                            waitDialog.dismiss()
                            runOnUiThread {
                                val tipsDialog = TipsDialog(this@FeedbackActivity)
                                tipsDialog.setInfo(
                                    getString(R.string.network_error),
                                    binding.dataEv
                                )
                                tipsDialog.show()
                            }
                        }

                    })

            }



        }

        // 观察 ViewModel 中的 LiveData，处理反馈提交结果
        viewModel.mFeedbackResponseBodyLiveData.observe(this) { response ->
            // 关闭等待对话框
            waitDialog.dismiss()
            // 根据提交结果显示提示对话框
            val messageResId = if (response.isSuccess) R.string.feedback_success else R.string.feedback_failure
            val tipsDialog = TipsDialog(this)
            tipsDialog.setInfo(getString(messageResId), binding.dataEv)
            tipsDialog.show()
        }
    }
}
