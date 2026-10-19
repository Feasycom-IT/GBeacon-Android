package com.glowbird.gbeacon.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.glowbird.gbeacon.R

/**
 * 提示对话框，用于显示简单的提示信息
 */
class TipsDialog(context: Context?) : BaseDialog(context) {

    // 控件
    private lateinit var tipsGot: Button
    private lateinit var dialogTips: TextView
    private lateinit var dialogTitle: TextView
    var dataEvs: EditText? = null

    // 初始化界面
    private fun initUI() {
        // 加载布局文件
        val v = layoutInflater.inflate(
            R.layout.dialog_tips, null, false
        )
        // 设置布局参数
        val lp = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 将布局添加到对话框中
        addContentView(v, lp)
        // 初始化控件并设置点击监听器
        tipsGot = findViewById<Button>(R.id.tips_got).apply {
            setOnClickListener {
                // 点击确认按钮，根据提示信息进行相应操作，并关闭对话框
                if (dialogTips.text == "反馈成功") {
                    dataEvs?.setText("")
                }
                dismiss()
            }
        }
        // 初始化提示信息控件
        dialogTips = findViewById(R.id.dialog_tips)
        // 初始化标题控件
        dialogTitle = findViewById(R.id.dialog_title)
        // 设置触摸外部区域不关闭对话框
        setCanceledOnTouchOutside(false)
    }

    // 设置提示信息
    fun setInfo(info: String?) {
        dialogTips.text = info
    }

    // 设置提示信息和关联的EditText
    fun setInfo(info: String, dataEv: EditText) {
        dialogTips.text = info
        dataEvs = dataEv
    }

    // 设置对话框标题
    fun setTitle(title: String?) {
        dialogTitle.text = title
        dialogTitle.visibility = View.VISIBLE
    }

    // 构造函数中初始化界面
    init {
        initUI()
    }
}
