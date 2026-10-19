package com.glowbird.gbeacon.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.feasycn.bean.BeaconBean
import com.glowbird.gbeacon.databinding.EddystoneUrlParameterSettingBinding
import com.glowbird.gbeacon.logic.utils.ViewUtil
import com.feasycn.util.FileUtil.*
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class Eddystone_URLView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var showType: Int? = 0
    private var mBeacon: BeaconBean? = null
    private var mBinding: EddystoneUrlParameterSettingBinding =
        EddystoneUrlParameterSettingBinding.inflate(LayoutInflater.from(context), this)
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

    fun setShowType(mShowType: Int) {
        showType = mShowType
    }

    init {
        mBinding.eddystoneUrl.addTextChangedListener(afterTextChanged = { s ->
            val header_url: String
            var header_url_foot: String
            val value = s.toString()
            try {
                if (value.contains("https://") || value.contains("https://www") || value.contains("http://www") || value.contains("http://")) {
                    header_url = getHeadByHex(value) // 提取 URL 头部
                    header_url_foot = getFootByHex(header_url) // 提取 URL 尾部
                    if (header_url == header_url_foot) {
                        if (header_url_foot.length <= 19) {
                            ViewUtil.setLabelEditBlock(
                                mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel
                            )
                            mBeacon?.url = value
                        } else {
                            ViewUtil.setLabelEditRed(
                                mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel
                            )
                            mBeacon?.url = ""
                        }
                    } else {
                        var len = 0
                        header_url_foot = header_url_foot.replace("00", "0e")
                        val foots = header_url_foot.split("0").toTypedArray()
                        for (i in 1 until foots.size) {
                            Log.e("TAG","foots[$i] => ${foots[i]}")
                            foots[i] = "0" + foots[i]
                        }
                        for (i in 1 until foots.size) {
                            len = len + foots[i].length - 2
                        }
                        len += foots.size
                        if (len <= 18) {
                            ViewUtil.setLabelEditBlock(
                                mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel
                            )
                            mBeacon?.url = value
                        } else {
                            ViewUtil.setLabelEditRed(
                                mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel
                            )
                            mBeacon?.url = ""
                        }
                    }
                } else {
                    ViewUtil.setLabelEditRed(mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel)
                    mBeacon?.url = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(mBinding.eddystoneUrl, mBinding.eddystoneUrlLabel)
                mBeacon?.url = ""
            }
        })

        mBinding.eddystoneRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() >= -128 && value.toInt() <= 127) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.eddystoneRssi, mBinding.eddystoneRssiLabel
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(mBinding.eddystoneRssi, mBinding.eddystoneRssiLabel)
                    mBeacon?.rssi = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(mBinding.eddystoneRssi, mBinding.eddystoneRssiLabel)
                mBeacon?.rssi = ""
            }
        })

        mBinding.eddystoneUrl.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.eddystoneRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.eddystoneUrl.setOnTouchListener { v, event ->
            false
        }

        mBinding.eddystoneRssi.setOnTouchListener { v, event ->
            false
        }
    }

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon
            mBinding.eddystoneRssi.addTextChangedListener(
                ViewUtil.RssiTextWatcher(
                    mBinding.eddystoneRssiLabel, mBinding.eddystoneRssi, beacon
                )
            )
            mBinding.beaconEnable.isChecked = true
            mBeacon?.isEnable = true

            mBinding.beaconEnable.setOnCheckedChangeListener { button, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {
        mBeacon = beacon
        setUrl(beacon.url)
        setRssi(beacon.rssi)
        mBinding.eddystoneRssi.addTextChangedListener(
            ViewUtil.RssiTextWatcher(
                mBinding.eddystoneRssiLabel, mBinding.eddystoneRssi, beacon
            )
        )
        setEnable(beacon.isEnable)
        mBinding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }
        if (!TextUtils.isEmpty(beacon.parentName) && beacon.parentName.equals("BP104EL")) {
            mBinding.urlIntervalLl.visibility = View.VISIBLE
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
            mBinding.urlInterval.setText(beacon.interval.toString())
            mBinding.urlInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.urlInterval, mBinding.urlIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
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
            mBinding.urlIntervalLl.visibility = View.VISIBLE
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
            mBinding.urlInterval.setText(beacon.interval.toString())
            mBinding.urlInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.urlInterval, mBinding.urlIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
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
            mBinding.urlIntervalLl.visibility = View.VISIBLE
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
            mBinding.urlInterval.setText(beacon.interval.toString())
            mBinding.urlInterval.addTextChangedListener(afterTextChanged = { s ->
                val value = s.toString().lowercase(Locale.getDefault())
                if (value.length in 1..5 && 100 <= Integer.valueOf(value) && Integer.valueOf(value) <= 10000) {
                    ViewUtil.setLabelEditBlock(mBinding.urlInterval, mBinding.urlIntervalLabel)
                    if (showType == 1) {
                        beacon.interval = Integer.valueOf(value)
                    } else {
                        beacon.interval = Integer.valueOf(value)
//                        FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                    }
                } else {
                    if (value.length in 1..5 && Integer.valueOf(value) < 100) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
                        if (showType == 1) {
                            beacon.interval = 100
                        } else {
                            beacon.interval = 100
//                            FscBeaconApiImp.getInstance().updateBeaconInfo(beacon, (beacon.index.toInt() - 1).toString())
                        }
                    } else if (value.length in 1..5 && Integer.valueOf(value) > 10000) {
                        ViewUtil.setLabelEditRed(mBinding.urlInterval, mBinding.urlIntervalLabel)
                        if (showType == 10) {
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

    private fun setUrl(temp: String?) {
        mBinding.eddystoneUrl.setText(temp)
    }

    private fun setRssi(temp: String?) {
        mBinding.eddystoneRssi.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.beaconEnable.isChecked = flag
    }

}