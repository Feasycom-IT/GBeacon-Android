package com.glowbird.gbeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.feasycn.bean.IBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.SearchIbeaconInfoBinding

/**
 * 自定义视图，用于显示 iBeacon 数据
 */
class IBeaconItem(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    fun setIBeaconValue(iBeacon: IBeacon?) {
        iBeacon?.let {
            try {
                mBinding.tvIbeaconCompany.text = "<${iBeacon.company}>"
                mBinding.tvIbeaconType.text = "<0x${iBeacon.type}>"
                mBinding.tvIbeaconLength.text = "${iBeacon.dataLength} bytes"
                mBinding.tvIbeaconUuid.text = iBeacon.uuid

                var majorString = try {
                    Integer.valueOf(iBeacon.major).toString()
                } catch (e: Exception) {
                    context.resources.getString(R.string.unknown)
                }
                mBinding.tvIbeaconMajor.text = majorString

                var minorString = try {
                    Integer.valueOf(iBeacon.minor).toString()
                } catch (e: Exception) {
                    context.resources.getString(R.string.unknown)
                }
                mBinding.tvIbeaconMinor.text = minorString

                var rssiString = try {
                    Integer.valueOf(iBeacon.rssi).toString()
                } catch (e: Exception) {
                    context.resources.getString(R.string.unknown)
                }
                mBinding.tvIbeaconRssi.text = "$rssiString dBm"

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var mBinding: SearchIbeaconInfoBinding =
        SearchIbeaconInfoBinding.inflate(LayoutInflater.from(context), this)

}