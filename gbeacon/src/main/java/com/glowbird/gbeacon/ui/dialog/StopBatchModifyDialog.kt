package com.glowbird.gbeacon.ui.dialog

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.glowbird.gbeacon.App
import com.glowbird.gbeacon.R

class StopBatchModifyDialog(context: Context) : BaseDialog(context), View.OnClickListener {

    // 取消按钮
    private lateinit var mCancelBt: Button
    // 确认按钮
    private lateinit var mConfirmBt: Button

    // 取消按钮点击监听器
    var mCancelOnClickListener: (() -> Unit)? = null
    // 确认按钮点击监听器
    var mConfirmOnClickListener: (() -> Unit)? = null

    // 倒计时定时器
    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 更新取消按钮上的倒计时文本
                mCancelBt.text = App.Companion.sContext.resources.getString(
                    R.string.confirm_stop, millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                // 倒计时结束后启用取消按钮
                mCancelBt.isEnabled = true
            }
        }
    }

    // 初始化界面
    private fun initUI() {
        // 加载对话框布局
        val v = layoutInflater.inflate(
            R.layout.dialog_stop_batch_modify_info, null, false
        )
        // 设置布局参数
        val lp = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        // 将布局添加到对话框中
        addContentView(v, lp)
        // 初始化取消按钮并设置点击监听器
        mCancelBt = findViewById(R.id.cancel_bt)
        mCancelBt.setOnClickListener(this)
        // 初始化确认按钮并设置点击监听器
        mConfirmBt = findViewById(R.id.confirm_bt)
        mConfirmBt.setOnClickListener(this)
        // 启动倒计时
        countDownTimer.start()
    }

    // 构造函数中初始化界面
    init {
        initUI()
    }

    // 显示对话框
    override fun show() {
        super.show()
        val lp = window?.attributes
        lp?.gravity = Gravity.CENTER
        lp?.width = ViewGroup.LayoutParams.MATCH_PARENT //宽高可设置具体大小
        lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = lp
    }

    // 点击事件处理
    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel_bt -> {
                // 点击取消按钮，执行取消操作并关闭对话框
                dismiss()
                mCancelOnClickListener?.invoke()
            }
            R.id.confirm_bt -> {
                // 点击确认按钮，执行确认操作并关闭对话框
                dismiss()
                mConfirmOnClickListener?.invoke()
            }
        }
    }

}
