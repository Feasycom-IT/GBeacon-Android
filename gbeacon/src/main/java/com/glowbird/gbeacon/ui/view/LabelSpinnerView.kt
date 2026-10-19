package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.feasycn.controler.FscBeaconApi
import com.feasycn.controler.FscBeaconApiImp
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.LableSpinnerViewBinding

class LabelSpinnerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private val mBinding: LableSpinnerViewBinding =
        LableSpinnerViewBinding.inflate(LayoutInflater.from(context), this)

    private var first = true
    private val fscBeaconApi: FscBeaconApi = FscBeaconApiImp.getInstance()
    private var mModel = ""
    private var showType: Int? = 0
    private lateinit var mLabelSpinnerCallBack: LabelSpinnerCallBack

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.parameterLabel.text = label
        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (showType != 0) {
                    mLabelSpinnerCallBack.selectCallback(position)
                    return
                }
                if (!first) {
                    if ("BP671" == mModel) {
                        fscBeaconApi.setDeviceTxPower(s[id.toInt()])
                    } else if ("BP106Q" == mModel) {
                        when(id.toInt()) {
                            0 -> fscBeaconApi.setDeviceTxPower("1F")
                            1 -> fscBeaconApi.setDeviceTxPower("23")
                            2 -> fscBeaconApi.setDeviceTxPower("26")
                        }

                    } else if ("BP104DL" == mModel) {
                        fscBeaconApi.setDeviceTxPower(stringsBP10DL[id.toInt()])
                    } else if ("BP104EL" == mModel) {
                        fscBeaconApi.setDeviceTxPower(stringsBP104EL[id.toInt()])
                    } else if (mModel.indexOf("BP104ELTX_") != -1) {
                        var eltxArray: List<String> = mModel.split("_")
                        if (eltxArray.isNotEmpty()) {
                            fscBeaconApi.setBroadcastTxPower(
                                ((eltxArray[1].toInt() - 1).toString()), id.toInt()
                            )
                        }
                    } else if (mModel.indexOf("BP104ELPHY_") != -1) {
                        var eltxArray: List<String> = mModel.split("_")
                        if (eltxArray.isNotEmpty()) {
                            fscBeaconApi.setBroadcastPHY(
                                ((eltxArray[1].toInt() - 1).toString()), id.toInt()
                            )
                        }
                    } else if ("BP104EA" == mModel) {
                        fscBeaconApi.setDeviceTxPower(stringsBP104EA[id.toInt()])
                    } else if (mModel.indexOf("BP104EATX_") != -1) {
                        var eltxArray: List<String> = mModel.split("_")
                        if (eltxArray.isNotEmpty()) {
                            fscBeaconApi.setBroadcastTxPower(
                                ((eltxArray[1].toInt() - 1).toString()), id.toInt()
                            )
                        }
                    } else if (mModel.indexOf("BP104EAPHY_") != -1) {
                        var eltxArray: List<String> = mModel.split("_")
                        if (eltxArray.isNotEmpty()) {
                            fscBeaconApi.setBroadcastPHY(
                                ((eltxArray[1].toInt() - 1).toString()), id.toInt()
                            )
                        }
                    } else if (mModel == "BP104E 2nd") {
                        fscBeaconApi.setDeviceTxPower(stringBP104E2nd[id.toInt()])
                    } else if (mModel.indexOf("BP104E 2ndTX_") != -1) {
                        var ndtxArray: List<String> = mModel.split("_")
                        if (ndtxArray.isNotEmpty()) {
                            Log.e("TAG","id => ${id.toInt()}")
                            fscBeaconApi.setBroadcastTxPower(
                                ((ndtxArray[1].toInt() - 1).toString()), stringBP104E2nd[id.toInt()]
                            )
                        }
                    } else if (mModel.indexOf("BP104E 2ndPHY_") != -1) {
                        var ndphyArray: List<String> = mModel.split("_")
                        if (ndphyArray.isNotEmpty()) {
                            fscBeaconApi.setBroadcastPHY(
                                ((ndphyArray[1].toInt() - 1).toString()), id.toInt()
                            )
                        }
                    } else if (id <= strings.size) {
                        Log.e(TAG,"strings[id.toInt() => ${strings[id.toInt()]}")
                        fscBeaconApi.setDeviceTxPower(strings[id.toInt()])
                    }



                } else {
                    first = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 未选择时的处理
            }

        }
        typedArray.recycle()
    }

    fun setRed() {
        mBinding.parameterLabel.setTextColor(resources.getColor(R.color.red))
    }

    fun setBlock() {
        mBinding.parameterLabel.setTextColor(-0xe2e2e3)
    }

    fun setShowType(mShowType: Int) {
        showType = mShowType
    }

    fun spinnerInit(
        spinnerAdapter: ArrayAdapter<String>,
        model: String,
        labelSpinnerCallBack: LabelSpinnerCallBack
    ) {
        mModel = model
        if (mModel.contains("BP104E 2ndPHY")) {
            mBinding.spinner.isEnabled = false
        }
        mBinding.spinner.adapter = spinnerAdapter
        mBinding.spinner.setSelection(0)
        mLabelSpinnerCallBack = labelSpinnerCallBack
    }

    fun setSelect(position: Int) {
        mBinding.spinner.setSelection(position)
    }

    val powerParameter: String get() = ""

    companion object {
        private const val TAG = "LabelSpinnerView"
        private val strings = arrayOf(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "10", "11", "12", "13", "14", "15", "16",
            "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F", "20"
        )

        private val stringsBP10DL = Array(21) { it.toString() }

        private val stringsBP104EL = Array(21) { it.toString() }

        private val stringsBP104EA = Array(11) { it.toString() }

        private val stringBP104E2nd = arrayOf(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"
        )

        private val s = arrayOf(
            "-200", "-107", "-95", "-82", "-70", "-65", "-5", "30", "45", "53", "60", "71", "80",
            "92", "102", "110", "120", "130", "141", "150", "160", "170", "181", "190", "200"
        )

    }
}
