package com.glowbird.gbeacon.ui.sensor

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.feasycn.bean.BluetoothDeviceWrapper
import com.glowbird.gbeacon.App
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentDeviceBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.DeviceType
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.logic.utils.getInt
import com.glowbird.gbeacon.logic.utils.getStr
import com.glowbird.gbeacon.ui.base.BaseFragment
import com.glowbird.gbeacon.ui.adapter.SensorAdapter
import com.glowbird.gbeacon.ui.filter.FilterActivity
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import java.lang.Exception

/**
 * 传感器Fragment，用于显示传感器设备列表和相关操作
 */
class SensorFragment : BaseFragment<FragmentDeviceBinding>(), FscBleCallback {

    // 存储检测到的
    private val mDevices = mutableListOf<BluetoothDeviceWrapper>()

    private lateinit var mFilterSensorName: String
    private var mFilterSensorRssi: Int = -100

    private val mDeviceAdapter: SensorAdapter by lazy {
        SensorAdapter(requireContext(), mDevices).apply {
            mOnItemClickListener = {}
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentDeviceBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        binding?.apply {
            header.headerTitle.text = getString(R.string.sensor)
            header.headerLeft.text = getString(R.string.sort)
            header.headerRight.text = getString(R.string.filter)
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = mDeviceAdapter
            // 设置 RecyclerView 间距
            recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.set(20, 20, 20, 20)
                }
            })
        }
    }

    private fun getiPermissionList(): MutableList<IPermission?> {
        val iPermissionList: MutableList<IPermission?> = ArrayList()
        iPermissionList.add(PermissionLists.getBluetoothAdvertisePermission())
        iPermissionList.add(PermissionLists.getBluetoothConnectPermission())
        iPermissionList.add(PermissionLists.getBluetoothScanPermission())
        iPermissionList.add(PermissionLists.getAccessFineLocationPermission())
        return iPermissionList
    }

    private fun CheckPermissions(context: Context): Boolean {
        return XXPermissions.isGrantedPermissions(context, getiPermissionList())
    }

    private fun CheckEnvironment(): Int {
        if (!CheckPermissions(requireActivity())) {
            return -3
        }
        return 0
    }

    override fun initEvent() {
        binding?.apply {
            // 底部导航栏点击事件
            foot.run {
                beaconNav.setOnClickListener { findNavController().navigate(R.id.beaconFragment) }
                settingNav.setOnClickListener { findNavController().navigate(R.id.settingFragment) }
                storeNav.setOnClickListener { findNavController().navigate(R.id.storeFragment) }
                forumNav.setOnClickListener { findNavController().navigate(R.id.forumFragment) }
                aboutNav.setOnClickListener { findNavController().navigate(R.id.about_nav) }
                navigationBar.check(R.id.sensor_nav)
            }

            // 排序按钮点击事件
            header.headerLeft.setOnClickListener {
                try {
                    mDevices.sortByDescending { it.rssi }
                    mDeviceAdapter.notifyItemRangeChanged(0, mDevices.size)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 过滤按钮点击事件
            header.headerRight.setOnClickListener {
                startActivity(Intent(requireActivity(), FilterActivity::class.java).apply {
                    putExtra("device_type", DeviceType.SENSOR)
                })
            }

            // 下拉刷新监听事件
            refreshableView.setOnRefreshListener {
                if (CheckEnvironment() == -3) {
                    ToastUtils.showLong(R.string.need_permission)
                    return@setOnRefreshListener
                }

                mDevices.clear()
                BluetoothRepository.stopScan()
                mDeviceAdapter.notifyDataSetChanged()

                mHandler.postDelayed({
                    if (isVisible) {
                        BluetoothRepository.startScan()
                        it.closeHeaderOrFooter()
                    }
                }, 500)
            }
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onSensor(device: BluetoothDeviceWrapper) {
        // 检查是否符合过滤条件，符合则更新UI
        if (device.rssi > mFilterSensorRssi) {
            if (mFilterSensorName.isNotEmpty()) {
                if (device.name == null) return
                if (!device.name.uppercase().contains(mFilterSensorName.uppercase())) return
            }
            requireActivity().runOnUiThread {
                val index = mDevices.indexOf(device)
                if (index == -1) {
                    mDevices.add(device)
                    mDeviceAdapter.notifyItemChanged(mDevices.size)
                } else {
                    mDevices[index] = device
                    mDeviceAdapter.notifyItemChanged(index, "")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFilterSensorName = App.sContext.getStr("device_${DeviceType.SENSOR}_name")
        mFilterSensorRssi = App.sContext.getInt("device_${DeviceType.SENSOR}_rssi", -100)

        binding?.refreshableView?.autoRefresh()
        binding?.foot?.navigationBar?.check(R.id.sensor_nav)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}