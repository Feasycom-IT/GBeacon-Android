package com.glowbird.gbeacon.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.arrayMapOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feasycn.bean.FeasyBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ItemBeaconBinding
import com.glowbird.gbeacon.logic.model.Beacon
import com.glowbird.gbeacon.logic.utils.ShowBroadcastStatus

class BeaconAdapter(private val context: Context, private val mBeaconList: MutableList<Beacon>) :
    RecyclerView.Adapter<BeaconAdapter.ViewHolder>() {

    var isFold = ShowBroadcastStatus.NONE

    var defaultState = View.GONE

    private val mBroadcastMap = arrayMapOf<String, MutableList<FeasyBeacon>>()

    private val mBroadcastTlmMap = arrayMapOf<String, MutableList<FeasyBeacon>>()

    private val mBroadcastAdapterMap = arrayMapOf<String, BroadcastAdapter>()

    private val mBroadcastTlmAdapterMap = arrayMapOf<String, BroadcastAdapter>()

    class ViewHolder(val binding: ItemBeaconBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = mBeaconList.size

    override fun getItemId(position: Int): Long {
        return mBeaconList[position].device.timestampNanos;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBeaconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        viewHolder.binding.root.setOnClickListener {
            viewHolder.adapterPosition
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val beacon = mBeaconList[position]
        with(holder.binding) {
            addressTv.text = beacon.device.address
            nameTv.text = beacon.device.completeLocalName ?: beacon.device.name ?: "unknown name"
            rssiTv.text = "${beacon.device.rssi}dBm"

            mBroadcastMap[beacon.device.address] = mutableListOf()

            mBroadcastTlmMap[beacon.device.address] = mutableListOf()

            val broadcastAdapter = BroadcastAdapter(mBroadcastMap[beacon.device.address]!!)
            mBroadcastAdapterMap[beacon.device.address] = broadcastAdapter

            val broadcastTlmAdapter = BroadcastAdapter(mBroadcastTlmMap[beacon.device.address]!!)
            mBroadcastTlmAdapterMap[beacon.device.address] = broadcastTlmAdapter

            broadcastList.layoutManager = LinearLayoutManager(context)
            broadcastList.adapter = broadcastAdapter

            gBeaconTlmList.layoutManager = LinearLayoutManager(context)
            gBeaconTlmList.adapter = broadcastTlmAdapter

            if (beacon.device.iBeacon != null) {
                if (!mBroadcastMap[beacon.device.address]!!.contains(beacon.device.iBeacon)) {
                    mBroadcastMap[beacon.device.address]!!.add(beacon.device.iBeacon)
                    broadcastList.adapter!!.notifyItemChanged(mBroadcastMap[beacon.device.address]!!.size)
                }
            }

            if (beacon.device.eddystoneBeacon != null) {
                if (beacon.device.eddystoneBeacon.frameTypeString.equals(FeasyBeacon.BEACON_TYPE_EDDYSTONE_TLM)) {
                    mBroadcastTlmMap[beacon.device.address]!!.clear()
                    mBroadcastTlmMap[beacon.device.address]!!.add(beacon.device.eddystoneBeacon)
                    gBeaconTlmList.adapter!!.notifyDataSetChanged()
                } else {
                    if (!mBroadcastMap[beacon.device.address]!!.contains(beacon.device.eddystoneBeacon)) {
                        mBroadcastMap[beacon.device.address]!!.add(beacon.device.eddystoneBeacon)
                        broadcastList.adapter!!.notifyItemChanged(mBroadcastMap[beacon.device.address]!!.size)
                    }
                }

            }
            if (beacon.device.altBeacon != null) {
                if (!mBroadcastMap[beacon.device.address]!!.contains(beacon.device.altBeacon)) {
                    mBroadcastMap[beacon.device.address]!!.add(beacon.device.altBeacon)
                    broadcastList.adapter!!.notifyItemChanged(mBroadcastMap[beacon.device.address]!!.size)
                }
            }

            val intervalNanos = beacon.intervalNanos / 1000000
            if (intervalNanos > 0) {
                intervalTv.text = "$intervalNanos ms <-> "
                intervalTv.setTextColor(context.resources.getColor(R.color.black))
            } else {
                intervalTv.text = "N/A <-> "
                intervalTv.setTextColor(context.resources.getColor(R.color.color_CCCCCC))
            }

            if (isFold == ShowBroadcastStatus.FOLD || isFold != beacon.foldStatus) {
                showBroadcast(beacon)
            } else {
                when (isFold) {
                    ShowBroadcastStatus.FOLD -> {
                        broadcastList.visibility = View.GONE
                        gBeaconTlmList.visibility = View.GONE
                    }
                    ShowBroadcastStatus.EXPANSION -> {
                        broadcastList.visibility = View.VISIBLE
                        gBeaconTlmList.visibility = View.VISIBLE
                    }
                    else -> {
                        // 默认显示
                        broadcastList.visibility = defaultState
                        gBeaconTlmList.visibility = defaultState
                    }
                }
            }

            holder.itemView.setOnClickListener {
                if (broadcastList.visibility == View.GONE) {
                    broadcastList.visibility = View.VISIBLE
                    gBeaconTlmList.visibility = View.VISIBLE
                    isFold = ShowBroadcastStatus.NONE
                    beacon.foldStatus = ShowBroadcastStatus.EXPANSION
                } else {
                    broadcastList.visibility = View.GONE
                    gBeaconTlmList.visibility = View.GONE
                    isFold = ShowBroadcastStatus.NONE
                    beacon.foldStatus = ShowBroadcastStatus.FOLD
                }
                notifyItemChanged(position, " ")
            }
        }
    }


    // 局部刷新
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any?>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }
        val beacon = mBeaconList[position]
        with(holder.binding) {
            rssiTv.text = "${beacon.device.rssi}dBm"
            val intervalNanos = beacon.intervalNanos / 1000000
            if (intervalNanos > 0) {
                intervalTv.text = "$intervalNanos ms <-> "
                intervalTv.setTextColor(context.resources.getColor(R.color.black))
            } else {
                intervalTv.text = "N/A <-> "
                intervalTv.setTextColor(context.resources.getColor(R.color.color_CCCCCC))
            }

            val mutableList = mBroadcastMap[beacon.device.address]
            val mutableTlmList = mBroadcastTlmMap[beacon.device.address]
            if (mutableTlmList != null) {
                if (beacon.device.iBeacon != null) {
                    if (!mutableList!!.contains(beacon.device.iBeacon)) {
                        mutableList.add(beacon.device.iBeacon)
                        broadcastList.adapter!!.notifyItemChanged(mutableList.size)
                    }
                }
                if (beacon.device.eddystoneBeacon != null) {
                    if (beacon.device.eddystoneBeacon.frameTypeString.equals("TLM")) {
                        mutableTlmList.clear()
                        mutableTlmList.add(beacon.device.eddystoneBeacon)
                        gBeaconTlmList.adapter!!.notifyDataSetChanged()
                    } else {
                        if (!mutableList!!.contains(beacon.device.eddystoneBeacon)) {
                            mutableList.add(beacon.device.eddystoneBeacon)
                            broadcastList.adapter!!.notifyItemChanged(mutableList.size)
                        }
                    }
                }
                if (beacon.device.altBeacon != null) {
                    if (!mutableList!!.contains(beacon.device.altBeacon)) {
                        mutableList.add(beacon.device.altBeacon)
                        broadcastList.adapter!!.notifyItemChanged(mutableList.size)
                    }
                }
                mBroadcastMap[beacon.device.address] = mutableList
                mBroadcastTlmMap[beacon.device.address] = mutableTlmList
            } else {
                val beaconList = mutableListOf<FeasyBeacon>()
                if (beacon.device.iBeacon != null) {
                    beaconList.add(beacon.device.iBeacon)
                    broadcastList.adapter!!.notifyItemChanged(beaconList.size)
                }
                if (beacon.device.eddystoneBeacon != null) {
                    beaconList.add(beacon.device.eddystoneBeacon)
                    broadcastList.adapter!!.notifyItemChanged(beaconList.size)
                }
                if (beacon.device.altBeacon != null) {
                    beaconList.add(beacon.device.altBeacon)
                    broadcastList.adapter!!.notifyItemChanged(beaconList.size)
                }
                mBroadcastMap[beacon.device.address] = beaconList
            }

            if (isFold != beacon.foldStatus) {
                showBroadcast(beacon)
            } else {
                when (isFold) {
                    ShowBroadcastStatus.FOLD -> {
                        broadcastList.visibility = View.GONE
                        gBeaconTlmList.visibility = View.GONE
                    }
                    ShowBroadcastStatus.EXPANSION -> {
                        broadcastList.visibility = View.VISIBLE
                        gBeaconTlmList.visibility = View.VISIBLE
                    }
                    ShowBroadcastStatus.NONE -> {
                        // 默认显示
                        broadcastList.visibility = defaultState
                        gBeaconTlmList.visibility = defaultState
                    }
                }
            }
        }
    }

    private fun ItemBeaconBinding.showBroadcast(beacon: Beacon) {
        when (beacon.foldStatus) {
            ShowBroadcastStatus.FOLD -> {
                broadcastList.visibility = View.GONE
                gBeaconTlmList.visibility = View.GONE
            }
            ShowBroadcastStatus.EXPANSION -> {
                broadcastList.visibility = View.VISIBLE
                gBeaconTlmList.visibility = View.VISIBLE
            }
            ShowBroadcastStatus.NONE -> {
                // 默认显示
                when (isFold) {
                    ShowBroadcastStatus.FOLD -> {
                        broadcastList.visibility = View.GONE
                        gBeaconTlmList.visibility = View.GONE
                    }
                    ShowBroadcastStatus.EXPANSION -> {
                        broadcastList.visibility = View.VISIBLE
                        gBeaconTlmList.visibility = View.VISIBLE
                    }
                    ShowBroadcastStatus.NONE -> {
                        // 默认显示
                        broadcastList.visibility = defaultState
                        gBeaconTlmList.visibility = defaultState
                    }
                }
            }
        }
    }

}