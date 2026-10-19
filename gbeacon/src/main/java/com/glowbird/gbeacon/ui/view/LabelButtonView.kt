package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.LableButtonViewBinding

class LabelButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private var mBinding: LableButtonViewBinding =
        LableButtonViewBinding.inflate(LayoutInflater.from(context), this)

    fun setCheck(enable: Boolean) {
        val currentTimeMillis = System.currentTimeMillis()
        mBinding.button.isChecked = enable
    }

    fun setOnToggleChanged(listener: SwitchButton.OnCheckedChangeListener) {
        mBinding.button.setOnCheckedChangeListener(listener)
    }

    val isOpen: Boolean
        get() = mBinding.button.isChecked

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.parameterLabel.text = label
        typedArray.recycle()
    }

}