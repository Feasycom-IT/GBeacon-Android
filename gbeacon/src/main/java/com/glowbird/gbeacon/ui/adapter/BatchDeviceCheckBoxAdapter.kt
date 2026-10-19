package com.glowbird.gbeacon.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.BatchDeviceCheckBooxInfoBinding
import com.glowbird.gbeacon.logic.model.BatchDevice

class BatchDeviceCheckBoxAdapter(private val deviceList: MutableList<BatchDevice>) :
    RecyclerView.Adapter<BatchDeviceCheckBoxAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 创建 ViewHolder 实例
        val binding = BatchDeviceCheckBooxInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = ViewHolder(binding)

        // 为根视图和复选框设置点击监听器
        viewHolder.binding.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position >= 0) {
                // 切换复选框状态
                deviceList[position].isClick = !deviceList[position].isClick
                // 通知数据集变化
                notifyItemChanged(position)
            }
        }
        viewHolder.binding.checkBox.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position >= 0) {
                // 切换复选框状态
                deviceList[position].isClick = !deviceList[position].isClick
                // 通知数据集变化
                notifyItemChanged(position)
            }
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 获取当前项的 BatchDevice 对象
        val batchDevice = deviceList[position]
        val device = batchDevice.device

        // 设置复选框状态
        holder.binding.checkBox.isChecked = batchDevice.isClick

        // 设置设备名称
        val deviceName = device.name ?: "unknown name"
        val completeName = device.completeLocalName
        val deviceAdd = device.address
        val formattedName = if (completeName != null && completeName.isNotEmpty()) {
            "$completeName-${deviceAdd.substring(9, 11)}${
                deviceAdd.substring(
                    12, 14
                )
            }${deviceAdd.substring(15, 17)}"
        } else if (deviceName.isNotEmpty()) {
            deviceName
        } else {
            "unknown name"
        }
        holder.binding.tvName.text = formattedName

        // 设置设备地址
        holder.binding.tvAddr.text = deviceAdd ?: "unknown address"

        // 设置RSSI
        holder.binding.tvRssi.text = "RSSI:${device.rssi}"

        // 设置信号强度进度条
        var deviceRssi = device.rssi.toInt()
        deviceRssi = when {
            deviceRssi <= -100 -> -100
            deviceRssi > 0 -> 0
            else -> deviceRssi
        }
        holder.binding.pbRssi.progress = 100 + deviceRssi


        // 设置电量图标和百分比
        device.feasyBeacon?.battery?.let { battery ->
            val batteryLevel = battery.toInt()
            val batteryResId = when {
                batteryLevel <= 0 -> R.drawable.electric_quantity0
                batteryLevel >= 100 -> R.drawable.electric_quantity_charging
                else -> R.drawable::class.java.getField("electric_quantity${batteryLevel / 10 * 10}")
                    .getInt(null)
            }
            holder.binding.chargePic.setImageResource(batteryResId)
            holder.binding.chargeValue.text = "$batteryLevel%"
        } ?: run {
            holder.binding.chargePic.setImageResource(R.drawable.electric_quantity_charging)
            holder.binding.chargeValue.text = "100%"
        }
    }

    override fun getItemCount() = deviceList.size

    // ViewHolder 内部类
    class ViewHolder(val binding: BatchDeviceCheckBooxInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

}