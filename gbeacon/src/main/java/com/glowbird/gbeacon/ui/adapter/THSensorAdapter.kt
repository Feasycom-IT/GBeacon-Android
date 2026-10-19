package com.glowbird.gbeacon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.THSensor
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ItemThsensorBinding

class THSensorAdapter(private val thSensorList: MutableList<THSensor>) :
    RecyclerView.Adapter<THSensorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemThsensorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemThsensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val thSensorDevice = thSensorList[position]
            temperatureTv.text = "${thSensorDevice.temperature}℃"
            humidityTv.text = "${thSensorDevice.humidity}%"
            when (val battery = thSensorDevice.battery) {
                // 根据电量百分比设置对应的图标和值
                in 0..100 -> {
                    // 根据电量百分比选择不同的图标
                    val resourceId = when {
                        battery <= 9 -> R.drawable.electric_quantity10
                        battery <= 19 -> R.drawable.electric_quantity20
                        battery <= 29 -> R.drawable.electric_quantity30
                        battery <= 39 -> R.drawable.electric_quantity40
                        battery <= 49 -> R.drawable.electric_quantity50
                        battery <= 59 -> R.drawable.electric_quantity60
                        battery <= 69 -> R.drawable.electric_quantity70
                        battery <= 79 -> R.drawable.electric_quantity80
                        battery <= 89 -> R.drawable.electric_quantity90
                        else -> R.drawable.electric_quantity100
                    }
                    chargePic.setImageResource(resourceId)
                    chargeValue.text = "$battery%"
                }
                // 充电中
                101 -> {
                    chargePic.setImageResource(R.drawable.electric_quantity_charging)
                    chargeValue.text = "100%"
                }
            }
        }
    }

    override fun getItemCount() = thSensorList.size
}