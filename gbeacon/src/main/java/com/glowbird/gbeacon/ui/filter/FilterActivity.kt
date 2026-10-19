package com.glowbird.gbeacon.ui.filter

import android.widget.SeekBar
import androidx.core.widget.addTextChangedListener
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityFilterBinding
import com.glowbird.gbeacon.logic.utils.getInt
import com.glowbird.gbeacon.logic.utils.getStr
import com.glowbird.gbeacon.logic.utils.putInt
import com.glowbird.gbeacon.logic.utils.putStr
import com.glowbird.gbeacon.ui.base.BaseActivity

class FilterActivity : BaseActivity<ActivityFilterBinding>() {

    // 设备类型
    private val mDeviceType by lazy {
        intent.getStringExtra("device_type")
    }

    // 设备名称
    private lateinit var mDeviceName: String

    // 设备RSSI
    private var mDeviceRssi = -100

    override fun getViewBinding() = ActivityFilterBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置标题栏
        binding.header.headerLeft.text = getString(R.string.back)
        binding.header.headerTitle.text = getString(R.string.filter)
        // 获取设备名称和RSSI
        mDeviceName = getStr("device_${mDeviceType}_name")
        mDeviceRssi = getInt("device_${mDeviceType}_rssi", -100)
        // 设置设备名称和RSSI
        binding.deviceNameEt.setText(mDeviceName)
        binding.deviceRssiTv.text = "$mDeviceRssi dB"
        binding.deviceRssiSeekBar.progress = mDeviceRssi + 100
    }

    override fun initEvent() {
        // 监听设备名称变化，并保存
        binding.deviceNameEt.addTextChangedListener {
            putStr("device_${mDeviceType}_name", it.toString())
        }
        // 监听设备RSSI变化，并保存
        binding.deviceRssiSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.deviceRssiTv.text = "${progress - 100} dB"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                putInt("device_${mDeviceType}_rssi", seekBar.progress - 100)
            }
        })
        // 返回按钮点击事件
        binding.header.headerLeft.setOnClickListener {
            finish()
        }
    }

}
