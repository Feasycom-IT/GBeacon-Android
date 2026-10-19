package com.glowbird.gbeacon.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.glowbird.gbeacon.databinding.DialogLoadBinding

class LoadDialogFragment(message: String) : DialogFragment() {

    // 加载提示信息
    var mMessage: String = message
        set(value) {
            mBinding.messageTv.text = value
            field = value
        }

    // 视图绑定
    private lateinit var mBinding: DialogLoadBinding

    // 创建对话框时设置对话框的属性和内容
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 使用Dialog对象创建对话框
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 设置对话框背景为透明
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 去除对话框标题栏
        dialog.setCancelable(false) // 设置对话框不可取消
        dialog.setCanceledOnTouchOutside(false) // 设置点击外部不可取消
        mBinding = DialogLoadBinding.inflate(layoutInflater) // 绑定视图
        dialog.setContentView(mBinding.root) // 设置对话框内容为绑定的视图
        mBinding.messageTv.text = mMessage // 设置加载提示信息
        return dialog
    }

    // 显示对话框，并确保在销毁状态时不会报错
    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed) return
        try {
            // 添加对话框前先移除可能存在的对话框，防止重复添加
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            // 捕获可能的异常，避免同一实例使用不同的tag引起的异常
            e.printStackTrace()
        }
    }

}