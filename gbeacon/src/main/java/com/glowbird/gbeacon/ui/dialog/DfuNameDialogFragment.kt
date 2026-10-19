package com.glowbird.gbeacon.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.glowbird.gbeacon.databinding.DfuNameFragmentBinding

class DfuNameDialogFragment : DialogFragment() {

    var onClickComplete: ((name: String) -> Unit)? = null // 点击确认回调函数

    private lateinit var mBinding: DfuNameFragmentBinding // 视图绑定

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = DfuNameFragmentBinding.inflate(inflater, container, false)

        // 设置对话框样式
        dialog?.let {
            it.window?.setBackgroundDrawable(ColorDrawable(0))
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置对话框可取消性
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
        }

        // 点击确认按钮时触发 onClickComplete 回调函数，并传入输入框中的文本
        mBinding.confirm.setOnClickListener {
            onClickComplete?.invoke(mBinding.dfuName.text.toString())
        }

        // 点击关闭按钮时隐藏对话框
        mBinding.close.setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun onResume() {
        // 设置对话框的宽度和高度
        dialog?.window!!.attributes = dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        } as WindowManager.LayoutParams

        super.onResume()
    }
}
