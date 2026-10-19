package com.glowbird.gbeacon.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.BluetoothDeviceWrapper
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.SettingDeviceInfoBinding

/**
 * 用于显示设置界面设备列表的适配器。
 * @param deviceList 包含要显示的设备列表。
 */
class SettingAdapter(private val deviceList: MutableList<BluetoothDeviceWrapper>) :
    RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {

    // 点击监听器
    var mOnItemClickListener: ((position: Int) -> Unit)? = null

    /**
     * 设置界面设备列表的 ViewHolder 类。
     * @param binding 与条目布局相关联的绑定对象。
     */
    class SettingViewHolder(val binding: SettingDeviceInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * 创建新的 ViewHolder 实例。
     * @param parent 新 View 将被添加到的 ViewGroup。
     * @param viewType 新 View 的视图类型。
     * @return 对应于填充的条目布局的 ViewHolder 实例。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = SettingDeviceInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = SettingViewHolder(binding)
        // 设置条目的点击事件
        viewHolder.binding.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position >= 0) {
                mOnItemClickListener?.invoke(position)
            }
        }
        return viewHolder
    }

    /**
     * 返回设备列表的总项目数。
     * @return 设备列表中的项目总数。
     */
    override fun getItemCount() = deviceList.size

    /**
     * 将数据绑定到指定位置的条目布局。
     * @param holder 应该更新以表示给定位置处项目内容的 ViewHolder。
     * @param position 项在适配器数据集中的位置。
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val device = deviceList[position]
        // 提取设备信息
        val name = device.completeLocalName ?: device.name ?: "unknown name"
        val deviceAdd = device.address
        var deviceRssi = device.rssi.toInt()
        // 将设备信息设置到视图中
        with(holder.binding) {
            // 设置设备名称
            if (name.isNotEmpty() && name != "unknown name") {
                tvName.text = name + "-" + deviceAdd.substring(9, 11) + deviceAdd.substring(
                    12, 14
                ) + deviceAdd.substring(15, 17)
            } else {
                tvName.text = "unknown name"
            }
            // 设置设备地址
            if (deviceAdd != null && deviceAdd.isNotEmpty()) {
                tvAddr.text = deviceAdd
            } else {
                tvAddr.text = "unknown address"
            }
            tvRssi.text = "RSSI:${device.rssi}"
            // 根据信号强度设置进度条
            when {
                deviceRssi <= -100 -> {
                    deviceRssi = -100
                }
                deviceRssi > 0 -> {
                    deviceRssi = 0
                }
            }
            pbRssi.progress = 100 + deviceRssi

            // 根据设备类型设置电量图标和电量值
            if (null != device.feasyBeacon) {
                val electricity = Integer.valueOf(device.feasyBeacon.battery).toInt()
                // 根据电量百分比选择不同的图标和值
                when {
                    electricity > 100 -> {
                        chargePic.setImageResource(R.drawable.electric_quantity_charging)
                        chargeValue.text = "100%"
                    }
                    electricity == 0 -> {
                        chargePic.setImageResource(R.drawable.electric_quantity0)
                        chargeValue.text = "0%"
                    }
                    else -> {
                        // 根据电量百分比选择不同的图标和值
                        val resourceId = when {
                            electricity in 1..9 -> R.drawable.electric_quantity10
                            electricity in 10..19 -> R.drawable.electric_quantity20
                            electricity in 20..29 -> R.drawable.electric_quantity30
                            electricity in 30..39 -> R.drawable.electric_quantity40
                            electricity in 40..49 -> R.drawable.electric_quantity50
                            electricity in 50..59 -> R.drawable.electric_quantity60
                            electricity in 60..69 -> R.drawable.electric_quantity70
                            electricity in 70..79 -> R.drawable.electric_quantity80
                            electricity in 80..89 -> R.drawable.electric_quantity90
                            else -> R.drawable.electric_quantity100
                        }
                        chargePic.setImageResource(resourceId)
                        chargeValue.text = "$electricity%"
                    }
                }
            } else {
                // 默认充电中
                chargePic.setImageResource(R.drawable.electric_quantity_charging)
                chargeValue.text = "100%"
            }
        }
    }

    /**
     * 局部刷新数据。
     * @param holder 应该更新的 ViewHolder。
     * @param position 项在适配器数据集中的位置。
     * @param payloads 要更新的项的有效负载。
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: SettingViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            // 如果有效负载为空，执行完整的 onBindViewHolder
            onBindViewHolder(holder, position)
        } else {
            // 如果有效负载不为空，执行局部刷新操作
            val device = deviceList[position]
            with(holder.binding) {
                // 更新信号强度和进度条
                tvRssi.text = "RSSI:${device.rssi}"
                pbRssi.progress = 100 + device.rssi
                // 根据设备类型更新电量图标和电量值
                if (null != device.feasyBeacon) {
                    val electricity = Integer.valueOf(device.feasyBeacon.battery).toInt()
                    // 根据电量百分比选择不同的图标和值
                    when {
                        electricity > 100 -> {
                            chargePic.setImageResource(R.drawable.electric_quantity_charging)
                            chargeValue.text = "100%"
                        }
                        electricity == 0 -> {
                            chargePic.setImageResource(R.drawable.electric_quantity0)
                            chargeValue.text = "0%"
                        }
                        else -> {
                            // 根据电量百分比选择不同的图标和值
                            val resourceId = when {
                                electricity in 1..9 -> R.drawable.electric_quantity10
                                electricity in 10..19 -> R.drawable.electric_quantity20
                                electricity in 20..29 -> R.drawable.electric_quantity30
                                electricity in 30..39 -> R.drawable.electric_quantity40
                                electricity in 40..49 -> R.drawable.electric_quantity50
                                electricity in 50..59 -> R.drawable.electric_quantity60
                                electricity in 60..69 -> R.drawable.electric_quantity70
                                electricity in 70..79 -> R.drawable.electric_quantity80
                                electricity in 80..89 -> R.drawable.electric_quantity90
                                else -> R.drawable.electric_quantity100
                            }
                            chargePic.setImageResource(resourceId)
                            chargeValue.text = "$electricity%"
                        }
                    }
                } else {
                    // 默认充电中
                    chargePic.setImageResource(R.drawable.electric_quantity_charging)
                    chargeValue.text = "100%"
                }
            }
        }
    }
}
