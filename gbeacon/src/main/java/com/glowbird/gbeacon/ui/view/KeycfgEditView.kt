package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.feasycn.controler.FscBeaconApi
import com.feasycn.controler.FscBeaconApiImp
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.KeycfgEditViewBinding
import com.glowbird.gbeacon.logic.model.DeviceInfo

class KeycfgEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val fscBeaconApi: FscBeaconApi = FscBeaconApiImp.getInstance()
    private var mBinding: KeycfgEditViewBinding = KeycfgEditViewBinding.inflate(LayoutInflater.from(context), this)

     var intervalMax = 0
     var intervalMin = 0
     var durationMax = 0
     var durationMin = 0

    init {
        getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView).apply {
            mBinding.intervalLabel.text = getString(R.styleable.LabelEditView_labelText)
            recycle()
        }
        setupListeners()
    }

    fun setRange(info: DeviceInfo) {
        val intervalRange = info.keyInterval.split("~")
        intervalMin = intervalRange[0].toInt()
        intervalMax = intervalRange[1].toInt()
        val durationRange = info.keyDuration.split("~")
        durationMin = durationRange[0].toInt()
        durationMax = durationRange[1].toInt()
    }

    fun setInterval(interval: String?) {
        mBinding.interval.setText(interval)
    }

    fun setDuration(duration: String?) {
        mBinding.duration.setText(duration)
    }

    fun getInterval(): Int {
        return Integer.valueOf(mBinding.interval.text.toString())
    }

    fun getDuration(): Int {
        return Integer.valueOf(mBinding.duration.text.toString())
    }

    private fun setupListeners() {
        mBinding.interval.setOnFocusChangeListener { _, hasFocus ->
            val intervalText = mBinding.interval.text.toString()

            if (!hasFocus && intervalText.isNotEmpty()) {
                val intervalValue = intervalText.toInt()

                if (intervalValue < intervalMin && intervalValue != 0) {
                    mBinding.interval.setText(intervalMin.toString())
                }
            }

            fscBeaconApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
        }

        mBinding.interval.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val intervalText = s.toString()
                if (intervalText.isNotEmpty()) {
                    val intervalValue = intervalText.toInt()

                    when {
                        intervalValue in intervalMin..intervalMax || intervalValue == 0 -> {
                            fscBeaconApi.setKeyConfig(intervalText, mBinding.duration.text.toString())
                        }
                        intervalValue > intervalMax -> {
                            mBinding.interval.setText(intervalMax.toString())
                            fscBeaconApi.setKeyConfig(intervalMax.toString(), mBinding.duration.text.toString())
                        }
                        else -> {
                            fscBeaconApi.setKeyConfig(intervalText, mBinding.duration.text.toString())
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        mBinding.duration.setOnFocusChangeListener { _, hasFocus ->
            val durationText = mBinding.duration.text.toString()
            if (!hasFocus) {
                if (durationText.isEmpty()) return@setOnFocusChangeListener
                val durationValue = durationText.toInt()

                if (durationValue < durationMin && durationValue != 0) {
                    mBinding.duration.setText(durationMin.toString())
                }
            }
            fscBeaconApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
        }

        mBinding.duration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val durationText = s.toString()
                if (durationText.isNotEmpty()) {
                    val durationValue = durationText.toInt()

                    when {
                        (durationValue in durationMin..durationMax || durationValue == 0) -> {
                            fscBeaconApi.setKeyConfig(mBinding.interval.text.toString(), durationText)
                        }
                        durationValue > durationMax -> {
                            mBinding.duration.setText(durationMax.toString())
                            fscBeaconApi.setKeyConfig(mBinding.interval.text.toString(), durationMax.toString())
                        }
                        else -> {
                            fscBeaconApi.setKeyConfig(mBinding.interval.text.toString(), durationText)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

}