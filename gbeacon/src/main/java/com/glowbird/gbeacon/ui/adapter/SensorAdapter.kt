package com.glowbird.gbeacon.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.arrayMapOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.bean.GSensor
import com.feasycn.bean.THSensor
import com.glowbird.gbeacon.databinding.ItemSensorBinding

/**
 * 用于显示传感器设备列表的适配器。
 * @param mSensorDeviceList 包含要显示的传感器设备的列表。
 */
class SensorAdapter(
    private val context: Context,
    private val mSensorDeviceList: MutableList<BluetoothDeviceWrapper>
) :
    RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    // 点击监听器
    var mOnItemClickListener: ((position: Int) -> Unit)? = null

    private val mTHSensorMap = arrayMapOf<String, MutableList<THSensor>>()

    private val mGSensorMap = arrayMapOf<String, MutableList<GSensor>>()

    private val mTHSensorAdapterMap = arrayMapOf<String, THSensorAdapter>()

    private val mGSensorAdapterMap = arrayMapOf<String, GSensorAdapter>()

    /**
     * 传感器设备列表的 ViewHolder 类。
     * @param binding 与条目布局相关联的绑定对象。
     */
    class SensorViewHolder(val binding: ItemSensorBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 返回传感器设备列表的总项目数。
     * @return 传感器设备列表中的项目总数。
     */
    override fun getItemCount() = mSensorDeviceList.size

    /**
     * 创建新的 ViewHolder 实例。
     * @param parent 新 View 将被添加到的 ViewGroup。
     * @param viewType 新 View 的视图类型。
     * @return 对应于填充的条目布局的 ViewHolder 实例。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = SensorViewHolder(binding)
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
     * 将数据绑定到指定位置的条目布局。
     * @param holder 应该更新以表示给定位置处项目内容的 ViewHolder。
     * @param position 项在适配器数据集中的位置。
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensorDevice = mSensorDeviceList[position]
        with(holder.binding) {
            nameTv.text = sensorDevice.completeLocalName ?: sensorDevice.name ?: "unknown name"
            addressTv.text = sensorDevice.address ?: "unknown address"
            rssiTv.text = "RSSI:${sensorDevice.rssi}dBm"

            mTHSensorMap[sensorDevice.address] = mutableListOf()

            mGSensorMap[sensorDevice.address] = mutableListOf()

            val thSensorAdapter = THSensorAdapter(mTHSensorMap[sensorDevice.address]!!)
            mTHSensorAdapterMap[sensorDevice.address] = thSensorAdapter

            val gSensorAdapter = GSensorAdapter(mGSensorMap[sensorDevice.address]!!)
            mGSensorAdapterMap[sensorDevice.address] = gSensorAdapter

            thSensorRv.layoutManager = LinearLayoutManager(context)
            thSensorRv.adapter = thSensorAdapter

            gSensorRv.layoutManager = LinearLayoutManager(context)
            gSensorRv.adapter = gSensorAdapter

            if (sensorDevice.thSensor != null) {
                if (!mTHSensorMap[sensorDevice.address]!!.contains(sensorDevice.thSensor)) {
                    mTHSensorMap[sensorDevice.address]!!.add(sensorDevice.thSensor)
                    thSensorRv.adapter!!.notifyItemChanged(mTHSensorMap[sensorDevice.address]!!.size)
                }
            }

            if (sensorDevice.gSensor != null) {
                if (!mGSensorMap[sensorDevice.address]!!.contains(sensorDevice.gSensor)) {
                    mGSensorMap[sensorDevice.address]!!.add(sensorDevice.gSensor)
                    gSensorRv.adapter!!.notifyItemChanged(mGSensorMap[sensorDevice.address]!!.size)
                }
            }

        }
    }

    // 局部刷新
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: SensorViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            // 如果有效负载为空，执行完整的 onBindViewHolder
            onBindViewHolder(holder, position)
        }
        val sensorDevice = mSensorDeviceList[position]
        with(holder.binding) {
            rssiTv.text = "RSSI:${sensorDevice.rssi}dBm"

            val thSensorMutableList = mTHSensorMap[sensorDevice.address]
            val gSensorMutableList = mGSensorMap[sensorDevice.address]
            if (thSensorMutableList != null) {
                if (sensorDevice.thSensor != null) {
                    if (!thSensorMutableList.contains(sensorDevice.thSensor)) {
                        thSensorMutableList.clear()
                        thSensorMutableList.add(sensorDevice.thSensor)
                        thSensorRv.adapter!!.notifyDataSetChanged()
                    }
                }
                if (sensorDevice.gSensor != null) {
                    if (!gSensorMutableList!!.contains(sensorDevice.gSensor)) {
                        gSensorMutableList.clear()
                        gSensorMutableList.add(sensorDevice.gSensor)
                        gSensorRv.adapter!!.notifyDataSetChanged()
                    }
                }
                mTHSensorMap[sensorDevice.address] = thSensorMutableList
                mGSensorMap[sensorDevice.address] = gSensorMutableList
            } else {
                val thSensorLists = mutableListOf<THSensor>()
                if (sensorDevice.thSensor != null) {
                    thSensorLists.add(sensorDevice.thSensor)
                    thSensorRv.adapter!!.notifyItemChanged(thSensorLists.size)
                }
                mTHSensorMap[sensorDevice.address] = thSensorLists
                val gSensorLists = mutableListOf<GSensor>()
                if (sensorDevice.gSensor != null) {
                    gSensorLists.add(sensorDevice.gSensor)
                    gSensorRv.adapter!!.notifyItemChanged(gSensorLists.size)
                }
                mGSensorMap[sensorDevice.address] = gSensorLists
            }
        }

    }

}
