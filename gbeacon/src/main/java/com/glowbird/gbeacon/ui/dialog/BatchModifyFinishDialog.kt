package com.glowbird.gbeacon.ui.dialog

import android.content.Context
import android.view.*
import android.widget.Button
import android.widget.RelativeLayout
import com.glowbird.gbeacon.R

/**
 * 批量修改完成对话框，继承自基础对话框类 BaseDialog
 */

class BatchModifyFinishDialog(context: Context) : BaseDialog(context), View.OnClickListener {

    // 取消按钮点击事件监听器
    var mCancelOnClickListener: (() -> Unit)? = null
    // 确认按钮点击事件监听器
    var mConfirmOnClickListener: (() -> Unit)? = null

    // 取消按钮
    lateinit var mCancelBt: Button
    // 确认按钮
    lateinit var mConfirmBt: Button

    /**
     * 初始化对话框布局和UI
     */
    private fun initUI() {
        val view = layoutInflater.inflate(R.layout.dialog_batch_modify_finish_info, null, false)
        val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // 添加对话框内容视图
        addContentView(view, layoutParams)
        // 初始化取消按钮并设置点击事件监听器
        mCancelBt = findViewById(R.id.cancel_bt)
        mCancelBt.setOnClickListener(this)
        // 初始化确认按钮并设置点击事件监听器
        mConfirmBt = findViewById(R.id.confirm_bt)
        mConfirmBt.setOnClickListener(this)
    }

    init {
        initUI()
    }

    /**
     * 显示对话框，并设置对话框位置为居中
     */
    override fun show() {
        super.show()
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.CENTER
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT //宽高可设置具体大小
        layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel_bt -> {
                // 点击取消按钮，隐藏对话框并触发取消事件监听器
                dismiss()
                mCancelOnClickListener?.invoke()
            }
            R.id.confirm_bt -> {
                // 点击确认按钮，隐藏对话框并触发确认事件监听器
                dismiss()
                mConfirmOnClickListener?.invoke()
            }
        }
    }

}