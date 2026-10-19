package com.glowbird.gbeacon.ui.dialog

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.glowbird.gbeacon.R

class InfoDialog(context: Context, private var mInfo: String) : BaseDialog(context as Activity) {

    private lateinit var dialogInfo: TextView // 对话框信息显示的 TextView
    private val mHandler = Handler(Looper.getMainLooper()) // 主线程 Handler

    // 初始化对话框 UI
    private fun initUI() {
        val v = layoutInflater.inflate(
            R.layout.dialog_loading, null, false
        )
        val lp = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addContentView(v, lp)
        dialogInfo = findViewById(R.id.dialog_info)
        setCanceledOnTouchOutside(false) // 设置对话框点击外部不消失
        setCancelable(false) // 设置对话框不可取消
        dialogInfo.text = mInfo // 设置对话框显示的信息
    }

    // 设置对话框显示的信息
    fun setInfo(info: String) {
        this.mInfo = info
        // 在主线程中更新对话框信息
        mHandler.post {
            dialogInfo.text = mInfo
        }
    }

    // 获取对话框当前显示的信息
    fun getInfo() = mInfo

    companion object {
        // 对话框显示时间常量
        const val INFO_DIALOG_SHOW_TIME = 1000
    }

    init {
        initUI()
    }

}
