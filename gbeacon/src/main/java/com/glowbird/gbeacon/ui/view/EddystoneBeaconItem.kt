package com.glowbird.gbeacon.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.feasycn.bean.EddystoneBeacon
import com.feasycn.bean.FeasyBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.SearchEddystoneInfoBinding
import com.glowbird.gbeacon.ui.utils.hexToSeconds
import com.glowbird.gbeacon.ui.utils.hexToTemperature
import com.glowbird.gbeacon.ui.utils.secondsToTime
import com.feasycn.util.FileUtil

/**
 * EddystoneBeaconItem 是一个自定义的 LinearLayout，用于显示 Eddystone Beacon 的信息
 */
class EddystoneBeaconItem(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var strData: String? = null

    private val mBinding: SearchEddystoneInfoBinding =
        SearchEddystoneInfoBinding.inflate(LayoutInflater.from(context), this)

    @SuppressLint("SetTextI18n")
    fun setEddyStoneBeaconValue(eddystoneBeacon: EddystoneBeacon?) {
        if (eddystoneBeacon != null) {
            strData = eddystoneBeacon.dataValue
            try {
                if (FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL == eddystoneBeacon.frameTypeString) {
                    with(mBinding) {
                        llEddystoneUrl.visibility = VISIBLE
                        llEddystoneUid.visibility = GONE
                        llEddystoneTlm.visibility = GONE
                        tvFrameType.text = "(${eddystoneBeacon.frameTypeString}) <0x${eddystoneBeacon.frameTypeHex}>"
                        tvEddystoneUrlRssi.text = "${Integer.valueOf(eddystoneBeacon.eddystoneRssiOrVersion)} dBm"
                        tvEddystoneUrl.text = strData
                        tvEddystoneUrl.setTextColor(resources.getColor(R.color.text_color_blue))
                        tvEddystoneUrl.paint.flags = Paint.UNDERLINE_TEXT_FLAG // 下划线
                    }
                } else if (FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID == eddystoneBeacon.frameTypeString) {
                    if (strData != null) {
                        for (i in strData!!.length..36) {
                            strData = "${strData}0"
                        }
                        with(mBinding) {
                            llEddystoneUrl.visibility = GONE
                            llEddystoneUid.visibility = VISIBLE
                            llEddystoneTlm.visibility = GONE
                            tvFrameType.text = "(${eddystoneBeacon.frameTypeString}) <0x${eddystoneBeacon.frameTypeHex}>"
                            tvEddystoneUidRssi.text = "${Integer.valueOf(eddystoneBeacon.eddystoneRssiOrVersion)} dBm"
                            tvEddystoneNamespace.text = "0x${eddystoneBeacon.nameSpace}"
                            tvEddystoneIntance.text = "0x${eddystoneBeacon.instance}"
                            tvEddystoneReserved.text = "0x${eddystoneBeacon.reserved}"
                        }
                    }
                } else if (FeasyBeacon.BEACON_TYPE_EDDYSTONE_TLM == eddystoneBeacon.frameTypeString) {
                    with(mBinding) {
                        llEddystoneUrl.visibility = GONE
                        llEddystoneUid.visibility = GONE
                        llEddystoneTlm.visibility = VISIBLE
                        tvFrameType.text = "(${eddystoneBeacon.frameTypeString}) <0x${eddystoneBeacon.frameTypeHex}>"
                        tvEddystoneTlmVersion.text = Integer.valueOf(eddystoneBeacon.eddystoneRssiOrVersion).toString()
                        tvEddystoneTlmBatteryVoltage.text = "${eddystoneBeacon.batteryVoltage.toInt(16)} mV"

                        val temperature = eddystoneBeacon.temperature
                        if (FileUtil.formattingHexToInt(temperature) == 128) {
                            tvEddystoneTlmTemperature.text = "Temperature not supported"
                        } else {
                            val decimalValue = hexToTemperature(temperature)
                            tvEddystoneTlmTemperature.text = "Temperature: $decimalValue ℃"
                        }
                        tvEddystoneTlmAdvertisementsCount.text = "${eddystoneBeacon.advertisementsCount.toInt(16)}"
                        tvEddystoneTlmTimeSincePowerUp.text = secondsToTime(hexToSeconds(eddystoneBeacon.timeSincePowerUp))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}