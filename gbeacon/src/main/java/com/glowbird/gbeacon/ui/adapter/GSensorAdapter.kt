package com.glowbird.gbeacon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.GSensor
import com.glowbird.gbeacon.databinding.ItemGsonsorBinding

class GSensorAdapter(private val gSensorList: MutableList<GSensor>) :
    RecyclerView.Adapter<GSensorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemGsonsorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGsonsorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val gSensorDevice = gSensorList[position]
            val xAxis = gSensorDevice.xAxis
            val yAxis = gSensorDevice.yAxis
            val zAxis = gSensorDevice.zAxis
            xAxisTv.text = "${xAxis.toDouble() / 1000} g"
            yAxisTv.text = "${yAxis.toDouble() / 1000} g"
            zAxisTv.text = "${zAxis.toDouble() / 1000} g"
        }
    }

    override fun getItemCount() = gSensorList.size

}