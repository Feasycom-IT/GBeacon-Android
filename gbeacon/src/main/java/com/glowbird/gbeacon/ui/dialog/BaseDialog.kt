package com.glowbird.gbeacon.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Window

/**
 * 自定义基础对话框，用于移除系统对话框的标题和边框背景
 */
open class BaseDialog : Dialog {
    constructor(context: Context?) : super(context!!) {
        /**
         * 移除系统对话框的标题和边框背景
         */
        window!!.setBackgroundDrawable(ColorDrawable(0))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    constructor(context: Context?, styles: Int) : super(context!!, styles) {
        /**
         * 移除系统对话框的标题和边框背景
         */
        window!!.setBackgroundDrawable(ColorDrawable(0))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

}