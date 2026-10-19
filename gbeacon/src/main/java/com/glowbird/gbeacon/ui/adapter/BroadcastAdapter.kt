package com.glowbird.gbeacon.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.FeasyBeacon
import com.feasycn.bean.IBeacon
import com.feasycn.bean.EddystoneBeacon
import com.feasycn.bean.AltBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ItemBroadcastBinding

/**
 * 用于在 RecyclerView 中显示不同类型的信标的适配器。
 * @param broadcastList 包含要显示的信标的 FeasyBeacon 对象列表。
 */
class BroadcastAdapter(private val broadcastList: MutableList<FeasyBeacon>) :
    RecyclerView.Adapter<BroadcastAdapter.ViewHolder>() {

    /**
     * 适配器的 ViewHolder 类。
     * @param binding 与条目布局相关联的绑定对象。
     */
    class ViewHolder(val binding: ItemBroadcastBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 填充条目布局并创建一个 ViewHolder 实例。
     * @param parent 新 View 将被添加到的 ViewGroup。
     * @param viewType 新 View 的视图类型。
     * @return 对应于填充的条目布局的 ViewHolder 实例。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBroadcastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * 将数据绑定到指定位置的条目布局。
     * @param holder 应该更新以表示给定位置处项目内容的 ViewHolder。
     * @param position 项在适配器数据集中的位置。
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            // 获取当前位置处的信标
            val beacon = broadcastList[position]

            // 根据信标类型设置相应的视图和数据
            when (beacon) {
                is IBeacon -> {
                    // 设置 IBeacon 数据和可见性
                    iBeaconItemView.setIBeaconValue(beacon)
                    iBeaconItemView.visibility = android.view.View.VISIBLE
                    // 设置背景图资源
                    beaconPic.setBackgroundResource(R.drawable.ibeacon)
                }
                is EddystoneBeacon -> {
                    // 设置 EddystoneBeacon 数据和可见性
                    gBeaconItemView.setEddyStoneBeaconValue(beacon)
                    gBeaconItemView.visibility = android.view.View.VISIBLE
                    // 根据帧类型设置背景图资源
                    beaconPic.setBackgroundResource(
                        when (beacon.frameTypeString) {
                            "URL" -> R.drawable.url
                            "UID" -> R.drawable.uid
                            else -> R.drawable.tlm
                        }
                    )
                }
                is AltBeacon -> {
                    // 设置 AltBeacon 数据和可见性
                    altBeaconItemView.setAltBeaconValue(beacon)
                    altBeaconItemView.visibility = android.view.View.VISIBLE
                    // 设置背景图资源
                    beaconPic.setBackgroundResource(R.drawable.altbeacon)
                }
                else -> { /* 其他信标类型不予处理 */ }
            }
        }
    }

    /**
     * 返回适配器数据集中的总项目数。
     * @return 数据集中的项目总数。
     */
    override fun getItemCount() = broadcastList.size
}
