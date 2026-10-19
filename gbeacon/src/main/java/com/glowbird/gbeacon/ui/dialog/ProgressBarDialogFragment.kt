package com.glowbird.gbeacon.ui.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.glowbird.gbeacon.R

class ProgressBarDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 设置对话框背景为透明
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        // 设置对话框背景的透明度为0
        dialog?.window?.setDimAmount(0F)
        // 去除对话框标题栏
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 加载对话框的布局
        return inflater.inflate(R.layout.progress_dialog_fragment, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 设置对话框外部点击不可取消
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed) return
        try {
            // 在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            // 同一实例使用不同的tag会异常，这里捕获一下
            e.printStackTrace()
        }
    }

}
