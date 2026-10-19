package com.glowbird.gbeacon.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityCrashBinding
import com.glowbird.gbeacon.ui.base.BaseActivity

/**
 * 崩溃捕捉界面
 */
class CrashActivity : BaseActivity<ActivityCrashBinding>() {

    private var mConfig: CaocConfig? = null
    private var mDialog: AlertDialog? = null

    override fun getViewBinding() = ActivityCrashBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mConfig = CustomActivityOnCrash.getConfigFromIntent(intent)
    }

    /**
     * 复制报错信息到剪贴板
     */
    private fun copyErrorToClipboard() {
        val errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(
            this, intent
        )
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText(
                "错误信息", errorInformation
            )
        )
    }

    override fun initView() {
        // 初始化界面视图
    }

    override fun initEvent() {
        // 初始化事件监听
        binding.btnCrashLog.setOnClickListener {
            showCrashDialog()
        }

        binding.btnCrashRestart.setOnClickListener {
            mConfig?.let {
                CustomActivityOnCrash.restartApplication(this, it)
            }
        }
    }

    /**
     * 显示崩溃对话框
     */
    private fun showCrashDialog() {
        if (mDialog == null) {
            mDialog = AlertDialog.Builder(this)
                .setTitle("错误信息")
                .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent))
                .setPositiveButton("关闭", null)
                .setNeutralButton("复制错误信息") { _, _ -> copyErrorToClipboard() }
                .create()
        }
        mDialog?.show()
        // 设置消息文本字体大小
        mDialog?.findViewById<TextView?>(R.id.message)?.textSize = 12f
    }
}
