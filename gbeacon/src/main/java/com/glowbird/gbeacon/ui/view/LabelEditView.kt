package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.LableEditViewBinding

class LabelEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    private var mBinding: LableEditViewBinding =
        LableEditViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.parameterLabel.text = label
        if ("Extend: " == label) {
            mBinding.parameter.visibility = GONE
        }
        typedArray.recycle()
    }

    fun setText(temp: String?) {
        mBinding.parameter.setText(temp)
    }

    fun setText(temp: String?, enable: Boolean) {
        mBinding.parameter.setText(temp)
        if (enable) {
            mBinding.parameter.setTextColor(resources.getColor(R.color.text_enable))
        } else {
            mBinding.parameter.setTextColor(resources.getColor(R.color.text_disable))
        }
        mBinding.parameter.isEnabled = enable
    }

    fun getText(): String {
        return mBinding.parameter.text.toString()
    }

    fun setRed() {
        mBinding.parameter.setTextColor(resources.getColor(R.color.red))
        mBinding.parameterLabel.setTextColor(resources.getColor(R.color.red))
    }

    fun setInputType(type: Int) {
        mBinding.parameter.inputType = type
    }

    fun setBlock() {
        mBinding.parameter.setTextColor(-0x848485)
        mBinding.parameterLabel.setTextColor(-0xe2e2e3)
    }

    fun setTextWatcher(textWatcher: TextWatcher?) {
        mBinding.parameter.addTextChangedListener(textWatcher)
    }

    fun check(): Boolean {
        return mBinding.parameterLabel.textColors !== ColorStateList.valueOf(resources.getColor(R.color.red))
    }

}