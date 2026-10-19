package com.glowbird.gbeacon.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.BatchDeviceInfoBinding
import com.glowbird.gbeacon.logic.model.BatchDevice

/**
 * 批量设备适配器，用于显示扫描到的设备信息列表
 */

class BatchDeviceAdapter(private val deviceList: MutableList<BatchDevice>) :
    RecyclerView.Adapter<BatchDeviceAdapter.ViewHolder>() {

    /**
     * 创建 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            BatchDeviceInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * 绑定 ViewHolder
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batchDevice = deviceList[position]
        val device = batchDevice.device
        val deviceName = device.name ?: "unknown name"
        val completeName = device.completeLocalName
        val deviceAdd = device.address
        var deviceRssi = device.rssi.toInt().coerceIn(-100, 0)

        // 设置设备名称
        holder.binding.tvName.text = if (!completeName.isNullOrEmpty()) {
            "$completeName-${deviceAdd.substring(9, 11)}${
                deviceAdd.substring(
                    12,
                    14
                )
            }${deviceAdd.substring(15, 17)}"
        } else {
            deviceName
        }

        // 设置设备地址
        holder.binding.tvAddr.text = deviceAdd ?: "unknown address"

        // 设置设备信号强度
        holder.binding.tvRssi.text = "RSSI:${device.rssi}"
        holder.binding.pbRssi.progress = 100 + deviceRssi

        // 设置电量图标和百分比
        device.feasyBeacon?.let { feasyBeacon ->
            val battery = feasyBeacon.battery?.toInt() ?: 100
            val batteryResId = when {
                battery > 90 -> R.drawable.electric_quantity100
                battery > 80 -> R.drawable.electric_quantity90
                battery > 70 -> R.drawable.electric_quantity80
                battery > 60 -> R.drawable.electric_quantity70
                battery > 50 -> R.drawable.electric_quantity60
                battery > 40 -> R.drawable.electric_quantity50
                battery > 30 -> R.drawable.electric_quantity40
                battery > 20 -> R.drawable.electric_quantity30
                battery > 10 -> R.drawable.electric_quantity20
                else -> R.drawable.electric_quantity10
            }
            holder.binding.chargePic.setImageResource(batteryResId)
            holder.binding.chargeValue.text = "$battery%"
        } ?: run {
            holder.binding.chargePic.setImageResource(R.drawable.electric_quantity_charging)
            holder.binding.chargeValue.text = "100%"

        }
    }

    /**
     * 返回数据项的数量。
     */
    override fun getItemCount() = deviceList.size

    /**
     * ViewHolder 类。
     */
    class ViewHolder(val binding: BatchDeviceInfoBinding) : RecyclerView.ViewHolder(binding.root)

}