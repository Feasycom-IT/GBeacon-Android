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
import com.glowbird.gbeacon.databinding.EddystoneUidParameterSettingBinding
import com.glowbird.gbeacon.logic.utils.ViewUtil
import java.lang.Exception
import java.util.*

/**
 * Eddystone_UIDView是用于展示Eddystone UID类型的Beacon的自定义View
 * @param context 上下文
 * @param attrs 属性集
 */
class Eddystone_UIDView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null
    private var showType: Int? = 0
    fun setShowType(mShowType: Int) {
        showType = mShowType
    }
    private lateinit var mpyhSpinnerAdapter: ArrayAdapter<String>
    private lateinit var mtxpowerSpinnerAdapter: ArrayAdapter<String>

    private val mTxPower104EL = listOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"
    )

    private val mTxPower104EA = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    private val mTxPower104E2nd = listOf(
        "-19.5", "-13.5", "-10", "-7", "-5", "-3.5", "-2", "-1", "0", "1", "1.5", "2.5"
    )

    private val pyhs = listOf("phy_1m", "phy_2m", "phy_coded")

    private var mBinding: EddystoneUidParameterSettingBinding =
        EddystoneUidParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    init {
        mBinding.eddystoneRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.eddystoneInstance.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.eddystoneNamespace.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.eddystoneReserved.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.eddystoneReserved.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(
                    mBinding.eddystoneReserved, mBinding.eddystoneReservedLabel
                )
                mBeacon?.reserved = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.eddystoneReserved, mBinding.eddystoneReservedLabel
                )
                mBeacon?.reserved = ""
            }
        })

        mBinding.eddystoneInstance.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 12) {
                ViewUtil.setLabelEditBlock(
                    mBinding.eddystoneInstance, mBinding.eddystoneInstanceLabel
                )
                mBeacon?.instance = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.eddystoneInstance, mBinding.eddystoneInstanceLabel
                )
                mBeacon?.instance = ""
            }
        })

        mBinding.eddystoneNamespace.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 20) {
                ViewUtil.setLabelEditBlock(
                    mBinding.eddystoneNamespace, mBinding.eddystoneNamespaceLabel
                )
                mBeacon?.nameSpace = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.eddystoneNamespace, mBinding.eddystoneNamespaceLabel
                )
                mBeacon?.nameSpace = ""
            }
        })

        mBinding.eddystoneRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() >= -128 && value.toInt() <= 127) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.eddystoneRssi, mBinding.eddystoneRssiLable
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(mBinding.eddystoneRssi, mBinding.eddystoneRssiLable)
                    mBeacon?.rssi = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(mBinding.eddystoneRssi, mBinding.eddystoneRssiLable)
                mBeacon?.rssi = ""
            }
        })

    }

    /**
     * 设置Beacon信息
     * @param beacon BeaconBean对象
     */
    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon
            mBinding.beaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    /**
     * 设置Beacon信息
     * @param beacon BeaconBean对象
     */
    fun setBeaconInfo(beacon: BeaconBean) {
        mBeacon = beacon
        setNameSpace(beacon.nameSpace)
        setInstance(beacon.instance)
        setReserved(beacon.reserved)
        setRssi(beacon.rssi)
        setEnable(beacon.isEnable)
        mBinding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }
        if (!TextUtils.isEmpty(beacon.parentName) && beacon.parentName.equals("BP104EL")) {
            mBinding.uidIntervalLl.visibility = View.VISIBLE
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
            mBinding.uidInterval.setText(beacon.interval.toString())
            mBinding.uidInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.uidInterval, mBinding.uidIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
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
            mBinding.uidIntervalLl.visibility = View.VISIBLE
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
            mBinding.phyLabelSpinner.spinnerInit(mpyhSpinnerAdapter,
                "BP104EAPHY_" + beacon.index,
                object : LabelSpinnerCallBack {
                    override fun selectCallback(p: Int) {
                        beacon.phy = p.toString()
                    }
                })
            mBinding.uidInterval.setText(beacon.interval.toString())
            mBinding.uidInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.uidInterval, mBinding.uidIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
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
            mBinding.uidIntervalLl.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.visibility = View.VISIBLE
            mBinding.phyLabelSpinner.visibility = View.VISIBLE
            mBinding.txPowerLabelSpinner.setShowType(1)
            mBinding.phyLabelSpinner.setShowType(1)
            mtxpowerSpinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mTxPower104E2nd)
            mBinding.txPowerLabelSpinner.spinnerInit(mtxpowerSpinnerAdapter,
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
            mBinding.uidInterval.setText(beacon.interval.toString())
            mBinding.uidInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.uidInterval, mBinding.uidIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.uidInterval, mBinding.uidIntervalLabel)
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

    /**
     * 设置Namespace
     * @param temp Namespace值
     */
    private fun setNameSpace(temp: String?) {
        mBinding.eddystoneNamespace.setText(temp)
    }

    /**
     * 设置Instance
     * @param temp Instance值
     */
    private fun setInstance(temp: String?) {
        mBinding.eddystoneInstance.setText(temp)
    }

    /**
     * 设置Reserved
     * @param temp Reserved值
     */
    private fun setReserved(temp: String?) {
        mBinding.eddystoneReserved.setText(temp)
    }

    /**
     * 设置Power
     * @param temp Power值
     */
    private fun setRssi(temp: String?) {
        mBinding.eddystoneRssi.setText(temp)
    }

    /**
     * 设置Enable状态
     * @param flag Enable状态
     */
    private fun setEnable(flag: Boolean) {
        mBinding.beaconEnable.isChecked = flag
    }

}