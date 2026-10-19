package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.feasycn.bean.BeaconBean
import com.glowbird.gbeacon.databinding.AltbeaconParameterSettingBinding
import com.glowbird.gbeacon.logic.utils.ViewUtil
import java.lang.Exception
import java.util.*

/**
 * 自定义View，用于显示AltBeacon的参数设置
 */

class AltBeaconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private var mBinding: AltbeaconParameterSettingBinding =
        AltbeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    private var mBeacon: BeaconBean? = null

    private var showType: Int? = 0

    fun setShowType(mshowType: Int) {
        showType = mshowType
    }

    private lateinit var mpyhSpinnerAdapter: ArrayAdapter<String>
    private lateinit var mtxpowerSpinnerAdapter: ArrayAdapter<String>

    private val mTxPower104EL = listOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"
    )

    private val mTxPower104EA = listOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
    )

    private val mTxPower104E2nd = listOf(
        "-19.5", "-13.5", "-10", "-7", "-5", "-3.5", "-2", "-1", "0", "1", "1.5", "2.5"
    )

    private val pyhs = listOf(
        "phy_1m", "phy_2m", "phy_coded"
    )

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon
            mBinding.beaconEnable.isChecked = true
            mBeacon?.isEnable = true
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {
        mBeacon = beacon
        mBinding.altBeaconId1.setText(beacon.id1)
        mBinding.altBeaconId2.setText(beacon.id2)
        mBinding.altBeaconId3.setText(beacon.id3)
        mBinding.altBeaconManufactureReserved.setText(beacon.manufacturerReserved)
        mBinding.altBeaconManufactureId.setText(beacon.manufacturerId)
        mBinding.altBeaconRssi.setText(beacon.rssi)
        mBinding.beaconEnable.isChecked = beacon.isEnable

        if (!TextUtils.isEmpty(beacon.parentName) && beacon.parentName.equals("BP104EL")) {
            mBinding.altbeaconIntervalLl.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.visibility = View.VISIBLE
            mBinding.phyLabelSpinner.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.setShowType(1)
            mBinding.phyLabelSpinner.setShowType(1)
            mtxpowerSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mTxPower104EL)
            mBinding.txPowerLabelSpinner.spinnerInit(mtxpowerSpinnerAdapter,
                "BP104ELTX_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.txpower = p.toString()
                    }
                })
            mpyhSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, pyhs)
            mBinding.phyLabelSpinner.spinnerInit(mpyhSpinnerAdapter,
                "BP104ELPHY_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.phy = p.toString()
                    }
                })
            mBinding.altbeaconInterval.setText(beacon.interval.toString())
            mBinding.altbeaconInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, ((beacon.index.toInt() - 1).toString()))
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 10000
                        } else {
                            beacon.interval = 10000
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    }
                }
            })
            mBinding.txPowerLabelSpinner.setSelect(beacon.txpower.toInt())
            mBinding.phyLabelSpinner.setSelect(beacon.phy.toInt())
        } else if (!TextUtils.isEmpty(beacon.parentName) && beacon.parentName.equals("BP104EA")) {
            mBinding.altbeaconInterval.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.visibility = View.VISIBLE
            mBinding.phyLabelSpinner.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.setShowType(1)
            mBinding.phyLabelSpinner.setShowType(1)
            mtxpowerSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mTxPower104EA)
            mBinding.txPowerLabelSpinner.spinnerInit(mtxpowerSpinnerAdapter,
                "BP104EATX_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.txpower = p.toString()
                    }
                })
            mpyhSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, pyhs)
            mBinding.phyLabelSpinner.spinnerInit(
                mpyhSpinnerAdapter,
                "BP104EAPHY_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.phy = p.toString()
                    }
                })
            mBinding.altbeaconInterval.setText(beacon.interval.toString())
            mBinding.altbeaconInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 10000
                        } else {
                            beacon.interval = 10000
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    }
                }
            })
            mBinding.txPowerLabelSpinner.setSelect(beacon.txpower.toInt())
            mBinding.phyLabelSpinner.setSelect(beacon.phy.toInt())
        } else if (!TextUtils.isEmpty(beacon.parentName) && beacon.parentName.equals("BP104E 2nd")) {
            mBinding.altbeaconIntervalLl.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.visibility = View.VISIBLE
            mBinding.phyLabelSpinner.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.setShowType(1)
            mBinding.phyLabelSpinner.setShowType(1)
            mtxpowerSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mTxPower104E2nd)
            mBinding.txPowerLabelSpinner.spinnerInit(
                mtxpowerSpinnerAdapter,
                "BP104E 2ndTX_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.txpower = when (p) {
                            0 -> "0"
                            1 -> "1"
                            2 -> "2"
                            3 -> "3"
                            4 -> "4"
                            5 -> "5"
                            6 -> "6"
                            7 -> "7"
                            8 -> "8"
                            9 -> "9"
                            10 -> "A"
                            11 -> "B"
                            else -> "0"
                        }
                    }
                })
            mpyhSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, pyhs)
            mBinding.phyLabelSpinner.spinnerInit(mpyhSpinnerAdapter,
                "BP104E 2ndPHY_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.phy = p.toString()
                    }
                })
            mBinding.phyLabelSpinner.setBackgroundColor(Color.LTGRAY)
            mBinding.altbeaconInterval.setText(beacon.interval.toString())
            mBinding.altbeaconInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.altbeaconInterval, mBinding.altbeaconIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 10000
                        } else {
                            beacon.interval = 10000
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    }
                }
            })
            when (beacon.txpower) {
                "0" -> mBinding.txPowerLabelSpinner.setSelect("0".toInt())
                "1" -> mBinding.txPowerLabelSpinner.setSelect("1".toInt())
                "2" -> mBinding.txPowerLabelSpinner.setSelect("2".toInt())
                "3" -> mBinding.txPowerLabelSpinner.setSelect("3".toInt())
                "4" -> mBinding.txPowerLabelSpinner.setSelect("4".toInt())
                "5" -> mBinding.txPowerLabelSpinner.setSelect("5".toInt())
                "6" -> mBinding.txPowerLabelSpinner.setSelect("6".toInt())
                "7" -> mBinding.txPowerLabelSpinner.setSelect("7".toInt())
                "8" -> mBinding.txPowerLabelSpinner.setSelect("8".toInt())
                "9" -> mBinding.txPowerLabelSpinner.setSelect("9".toInt())
                "A" -> mBinding.txPowerLabelSpinner.setSelect("10".toInt())
                "B" -> mBinding.txPowerLabelSpinner.setSelect("11".toInt())
            }
            mBinding.phyLabelSpinner.setSelect(beacon.phy.toInt())
        }
    }

    init {
        mBinding.altBeaconId1.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconId2.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconId3.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconManufactureReserved.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconManufactureId.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.altBeaconManufactureReserved.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 2) {
                ViewUtil.setLabelEditBlock(
                    mBinding.altBeaconManufactureReserved,
                    mBinding.altBeaconManufactureReservedLabel
                )
                mBeacon?.manufacturerReserved = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.altBeaconManufactureReserved,
                    mBinding.altBeaconManufactureReservedLabel
                )
                mBeacon?.manufacturerReserved = ""
            }
        })
        mBinding.altBeaconManufactureId.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length in 1..5 && Integer.valueOf(value) <= 65535) {
                ViewUtil.setLabelEditBlock(
                    mBinding.altBeaconManufactureId, mBinding.altBeaconManufactureIdLabel
                )
                mBeacon?.manufacturerId = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.altBeaconManufactureId, mBinding.altBeaconManufactureIdLabel
                )
                mBeacon?.manufacturerId = ""
            }
        })

        mBinding.altBeaconId1.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 32) {
                ViewUtil.setLabelEditBlock(mBinding.altBeaconId1, mBinding.altBeaconUuidLabel)
                mBeacon?.id1 = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.altBeaconId1, mBinding.altBeaconUuidLabel)
                mBeacon?.id1 = ""
            }
        })

        mBinding.altBeaconId2.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(mBinding.altBeaconId2, mBinding.altBeaconMajorLabel)
                mBeacon?.id2 = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.altBeaconId2, mBinding.altBeaconMajorLabel)
                mBeacon?.id2 = ""
            }
        })

        mBinding.altBeaconId3.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(mBinding.altBeaconId3, mBinding.altBeaconMinorLabel)
                mBeacon?.id3 = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.altBeaconId3, mBinding.altBeaconMinorLabel)
                mBeacon?.id3 = ""
            }
        })

        mBinding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }

        mBinding.altBeaconRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() <= 127 && value.toInt() >= -128) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.altBeaconRssi, mBinding.altBeaconRssiLabel
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(mBinding.altBeaconRssi, mBinding.altBeaconRssiLabel)
                    mBeacon?.rssi = ""
                }

            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(mBinding.altBeaconRssi, mBinding.altBeaconRssiLabel)
                mBeacon?.rssi = ""
            }
        })
    }

    companion object {
        const val TAG = "AltBeaconView"
    }

}