package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.feasycn.controler.FscBeaconApi
import com.feasycn.controler.FscBeaconApiImp
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.IntervalEditViewBinding

class IntervalEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mBinding: IntervalEditViewBinding =
        IntervalEditViewBinding.inflate(LayoutInflater.from(context), this)

    private val fscBeaconApi: FscBeaconApi = FscBeaconApiImp.getInstance()

    var intervalMax = 10 * 1000
    var intervalMin = 100

    private var first = true

    fun setRange(intervalRange: Array<String>) {
        intervalMin = intervalRange[0].toInt()
        intervalMax = intervalRange[1].toInt()
    }

    fun setInterval(interval: String?) {
        mBinding.intervalEdit.setText(interval)
    }

    val interval: Int
        get() = mBinding.intervalEdit.text.toString().toInt()

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.intervalLabel.text = label
        typedArray.recycle()

        mBinding.intervalEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val input = s.toString().toIntOrNull() ?: return
                when {
                    input == 0 || (input in 100..10000) -> {
                        mBinding.intervalEdit.setTextColor(Color.BLACK)
                        if (!first) {
                            fscBeaconApi.setBroadcastInterval(input.toString())
                        } else {
                            first = false
                        }
                    }
                    input > 10000 -> {
                        mBinding.intervalEdit.setTextColor(Color.RED)
                    }
                    else -> {
                        mBinding.intervalEdit.setTextColor(Color.RED)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        mBinding.intervalEdit.onFocusChangeListener = OnFocusChangeListener { v1: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                val inputText = mBinding.intervalEdit.text.toString()
                val inputValue = inputText.toIntOrNull() ?: intervalMin
                if (inputValue < intervalMin && inputValue != 0) {
                    mBinding.intervalEdit.setText(intervalMin.toString())
                }
                fscBeaconApi.setBroadcastInterval(mBinding.intervalEdit.text.toString())
            }

            if (!hasFocus) {
                if (mBinding.intervalEdit.text.toString().isEmpty()) {
                    return@OnFocusChangeListener
                }
                if (mBinding.intervalEdit.text.toString().isEmpty() || mBinding.intervalEdit.text.toString().toInt() < intervalMin && mBinding.intervalEdit.text.toString().toInt() != 0) {
                    mBinding.intervalEdit.setText(intervalMin.toString())
                } else {
                    fscBeaconApi.setBroadcastInterval(mBinding.intervalEdit.text.toString())
                }
            } else {
                fscBeaconApi.setBroadcastInterval(mBinding.intervalEdit.text.toString())
            }

        }
    }

}