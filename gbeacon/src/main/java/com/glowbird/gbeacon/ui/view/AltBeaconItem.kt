package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.feasycn.bean.AltBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.SearchAltbeaconInfoBinding
import java.util.*

/**
 * 自定义视图，用于显示AltBeacon的信息
 */

class AltBeaconItem(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mBinding: SearchAltbeaconInfoBinding =
        SearchAltbeaconInfoBinding.inflate(LayoutInflater.from(context), this)

    /**
     * 设置AltBeacon数据
     *
     * @param altBeacon AltBeacon对象
     */
    fun setAltBeaconValue(altBeacon: AltBeacon?) {
        if (null != altBeacon) {
            try {
                mBinding.tvAltBeaconCompany.text = "<${altBeacon.reservedId}>"
                try {
                    mBinding.tvAltBeaconType.text = "<${altBeacon.manufacturerId}>"
                } catch (e: Exception) {
                    e.printStackTrace()
                    mBinding.tvAltBeaconType.text = "0"
                }
                var temp: String = altBeacon.beaconId.lowercase(Locale.getDefault())
                temp = try {
                    temp.substring(0, 32) + " - " + temp.substring(32, temp.length)
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.resources.getString(R.string.unknown)
                }
                mBinding.tvAltBeaconBeaconId.text = temp
                try {
                    mBinding.tvAltbeaconRssi.text =
                        "${Integer.valueOf(altBeacon.altBeaconRssi)} dBm"
                } catch (e: Exception) {
                    mBinding.tvAltbeaconRssi.text = "null"
                }
                try {
                    mBinding.tvAltBeaconFeature.text =
                        "0x${altBeacon.feature.lowercase(Locale.getDefault())}"
                } catch (e: Exception) {
                    mBinding.tvAltBeaconFeature.text = "null"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}