package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.feasycn.controler.FscBeaconApi
import com.feasycn.controler.FscBeaconApiImp
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.GsensorSpinnerViewBinding
import com.glowbird.gbeacon.logic.model.DeviceInfo

class GsensorEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    var advinMin: Int = 0
    var advinMax: Int = 0
    var durationMin: Int = 0
    var durationMax: Int = 0
    var sensitivityMin: Int = 0
    var sensitivityMax: Int = 0
    private val fscBeaconApi: FscBeaconApi = FscBeaconApiImp.getInstance()
    private var mBinding: GsensorSpinnerViewBinding =
        GsensorSpinnerViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        mBinding.intervalLabel.text = typedArray.getString(R.styleable.LabelEditView_labelText)
        typedArray.recycle()

        setupEditTextListeners()
    }

    fun setRange(deviceInfo: DeviceInfo) {
        deviceInfo.gsensorInterval.split("~").apply {
            advinMin = getOrNull(0)?.toIntOrNull() ?: 0
            advinMax = getOrNull(1)?.toIntOrNull() ?: 0
        }
        deviceInfo.gsensorDuration.split("~").apply {
            durationMin = getOrNull(0)?.toIntOrNull() ?: 0
            durationMax = getOrNull(1)?.toIntOrNull() ?: 0
        }
        deviceInfo.gsensorSensitivity.split("~").apply {
            sensitivityMin = getOrNull(0)?.toIntOrNull() ?: 0
            sensitivityMax = getOrNull(1)?.toIntOrNull() ?: 0
        }
    }

    fun setAdvin(advin: Int) {
        mBinding.advinEdit.setText(advin.toString())
    }

    fun getAdvin(): Int = mBinding.advinEdit.text.toString().toIntOrNull() ?: 0

    fun setDuration(duration: Int) {
        mBinding.durationEdit.setText(duration.toString())
    }

    fun getDuration(): Int = mBinding.durationEdit.text.toString().toIntOrNull() ?: durationMin

    fun setSensitivityEdit(sensitivity: Int) {
        mBinding.sensitivityEdit.setText(sensitivity.toString())
        mBinding.sensitivityEdit.visibility = VISIBLE
    }

    fun getSensitivity(): Int = mBinding.sensitivityEdit.text.toString().toIntOrNull() ?: sensitivityMin

    private fun setGSensorConfig(broadcastInterval: String?, broadcastDuration: String?) {
        fscBeaconApi.setGSensorConfig(broadcastInterval, broadcastDuration)
    }

    private fun setGSensorConfig(
        broadcastInterval: String?, broadcastDuration: String?, threshold: String?
    ) {
        fscBeaconApi.setGSensorConfig(broadcastInterval, broadcastDuration, threshold)
    }

    companion object {
        var gsensorSend = false
    }

    private fun setupEditTextListeners() {
        mBinding.advinEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            if (s.toString().isNotEmpty() && (s.toString().toInt() >= advinMin!! || s.toString()
                    .toInt() == 0) && s.toString().toInt() <= advinMax!!
            ) {
                if (mBinding.sensitivityEdit.visibility == GONE) {
                    setGSensorConfig(s.toString(), mBinding.durationEdit.text.toString())
                } else {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(),
                        mBinding.durationEdit.text.toString(),
                        mBinding.sensitivityEdit.text.toString()
                    )
                }
            } else {
                if (s.toString().isNotEmpty() && s.toString().toInt() > advinMax!!) {
                    mBinding.advinEdit.setText(advinMax.toString())
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                    mBinding.advinEdit.setSelection(mBinding.advinEdit.text.toString().length)
                }
            }
        })

        mBinding.advinEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!mBinding.advinEdit.text.toString().isEmpty()) {
                    val integer = mBinding.advinEdit.text.toString().toInt()
                    if (integer >= 2000) {
                        gsensorSend = true
                    }
                }

                if (mBinding.advinEdit.text.toString()
                        .isEmpty() || mBinding.advinEdit.text.toString()
                        .toInt() < advinMin!! && mBinding.advinEdit.text.toString().toInt() != 0
                ) {
                    mBinding.advinEdit.setText(advinMin.toString())
                    mBinding.advinEdit.setSelection(mBinding.advinEdit.text.toString().length)
                }
            } else {
                if (mBinding.sensitivityEdit.visibility == GONE) {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(), mBinding.durationEdit.text.toString()
                    )
                } else {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(),
                        mBinding.durationEdit.text.toString(),
                        mBinding.sensitivityEdit.text.toString()
                    )
                }
            }
        }

        mBinding.durationEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            val durationText = s.toString()
            if (durationText.isNotEmpty()) {
                val durationValue = durationText.toLong()
                if (durationValue in durationMin..durationMax) {
                    setConfigBasedOnVisibility(durationText)
                } else if (durationValue > durationMax) {
                    mBinding.durationEdit.setText(durationMax.toString())
                    setConfigBasedOnVisibility(durationMax.toString())
                    mBinding.durationEdit.setSelection(durationMax.toString().length)
                }
            }
        })


        mBinding.durationEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (mBinding.durationEdit.text.toString()
                        .isEmpty() || mBinding.durationEdit.text.toString().toInt() < durationMin!!
                ) {
                    mBinding.durationEdit.setText(durationMin.toString())
                    mBinding.durationEdit.setSelection(mBinding.durationEdit.text.toString().length)
                } else {
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                }
            }
        }

        mBinding.sensitivityEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            if (s.toString().isNotEmpty() && s.toString()
                    .toInt() >= sensitivityMin!! && s.toString().toInt() <= sensitivityMax!!
            ) {
                setGSensorConfig(
                    mBinding.advinEdit.text.toString(),
                    mBinding.durationEdit.text.toString(),
                    s.toString()
                )
            } else {
                if (mBinding.sensitivityEdit.text.toString().isNotEmpty() && s.toString()
                        .toInt() > sensitivityMin!!
                ) {
                    mBinding.sensitivityEdit.setText(sensitivityMax.toString())
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                    mBinding.durationEdit.setSelection(mBinding.durationEdit.text.toString().length)
                }
            }
        })

        mBinding.sensitivityEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (mBinding.sensitivityEdit.text.toString()
                        .isEmpty() || mBinding.sensitivityEdit.text.toString()
                        .toInt() < sensitivityMin!!
                ) {
                    mBinding.sensitivityEdit.setText(sensitivityMin.toString())
                    mBinding.sensitivityEdit.setSelection(mBinding.sensitivityEdit.text.toString().length)
                } else {
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                }
            }
        }
    }

    // Helper function to simplify configuration setting
    private fun setConfigBasedOnVisibility(duration: String) {
        if (mBinding.sensitivityEdit.visibility == GONE) {
            setGSensorConfig(mBinding.advinEdit.text.toString(), duration)
        } else {
            setGSensorConfig(
                mBinding.advinEdit.text.toString(),
                duration,
                mBinding.sensitivityEdit.text.toString()
            )
        }
    }

}