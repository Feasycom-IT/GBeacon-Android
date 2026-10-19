package com.glowbird.gbeacon.ui.beacon

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycn.bean.BluetoothDeviceWrapper
import com.glowbird.gbeacon.App
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.FragmentBeaconBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.DeviceType
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.logic.model.Beacon
import com.glowbird.gbeacon.logic.utils.*
import com.glowbird.gbeacon.ui.adapter.BeaconAdapter
import com.glowbird.gbeacon.ui.base.BaseFragment
import com.glowbird.gbeacon.ui.filter.FilterActivity
import com.glowbird.gbeacon.ui.utils.BeaconDataManager
import com.glowbird.gbeacon.ui.utils.isLocationEnabled
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BeaconFragment : BaseFragment<FragmentBeaconBinding>(), FscBleCallback {

    // 存储检测到的 Beacon 列表
    private val beaconList = mutableListOf<Beacon>()

    // 过滤器中的 Beacon 名称
    private var filterBeaconName = ""

    // 过滤器中的 Beacon 信号强度
    private var filterBeaconRssi: Int = -100

    // Beacon 适配器
    private val deviceAdapter by lazy { BeaconAdapter(requireContext(), beaconList) }

    private val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.feasycom.feasybeacon"

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentBeaconBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        val storedHash = requireActivity().getStr("hash", "")
        Log.e("TAG", "BeaconFragment storeHash => $storedHash")
        MainScope().launch(Dispatchers.IO) {
            BeaconDataManager.fetchAndStoreBeaconData(App.sContext, storedHash, App.mBeaconItemDao)
        }
        // 设置界面元素
        binding?.apply {
            header.headerTitle.text = getString(R.string.beacon)
            header.headerLeft.text = getString(R.string.sort)
            header.headerRight.text = getString(R.string.filter)
            header.customBroadcastTv.visibility = View.VISIBLE
            header.customBroadcastTv.text = getString(R.string.custom_broadcast)
            header.customBroadcastTv.setOnClickListener {
                // 跳转到自定义广播界面
                startActivity(Intent(requireActivity(), CustomBroadcastActivity::class.java).apply {
//                    putExtra("device_type", DeviceType.BEACON)
                })
            }
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = deviceAdapter
            // 设置 RecyclerView 间距
            recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.set(20, 20, 20, 20)
                }
            })
            broadcastStatus.text =
                getString(if (deviceAdapter.defaultState == View.GONE) R.string.expansion else R.string.fold)
        }
        checkAppVersion()
    }


    private fun checkAppVersion() {
        try {
            // 获取本地应用版本号
            val packageInfo: PackageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val localVersionCode = packageInfo.versionCode

            // 初始化 AppUpdateManager
            val appUpdateManager = AppUpdateManagerFactory.create(requireActivity())

            // 获取 Google Play 更新信息
            val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val latestVersionCode: Int = appUpdateInfo.availableVersionCode()
                        Log.d(
                            "TAG_CJG",
                            "local version => $localVersionCode  Google Play Latest version number => $latestVersionCode"
                        )
                        if (localVersionCode < latestVersionCode) {
                            showUpdateDialog()
                        }
                    }

                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        Log.d("TAG_CJG", "No update available")
                    }

                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        Log.d("TAG_CJG", "Update in progress")
                    }

                    UpdateAvailability.UNKNOWN -> {
                        Log.d("TAG_CJG", "Update availability unknown")
                    }

                    else -> {
                        Log.d("TAG_CJG", "Unhandled update availability state")
                    }
                }
            }

            appUpdateInfoTask.addOnFailureListener { e ->
                Log.e("TAG_CJG", "Failed to obtain update information: $e")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG_CJG", "Failed to obtain local version information: $e")
        }
    }

    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.version_update))
        builder.setMessage(getString(R.string.update_message))
        builder.setPositiveButton(getString(R.string.Update)) { _, _ ->
            // 跳转到应用商店
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(PLAY_STORE_URL))
            startActivity(intent)
        }
        builder.setNegativeButton(getString(R.string.talk_later)) { dialog, _ ->
            // 用户选择稍后再说
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun initEvent() {
        // 设置界面事件监听
        binding?.apply {
            foot.run {
                sensorNav.setOnClickListener {
                    if (CheckEnvironment() == -3) {
                        ToastUtils.showLong(R.string.need_permission)
                    } else {
                        findNavController().navigate(R.id.sensorFragment)
                    }
                }
                settingNav.setOnClickListener {
                    if (CheckEnvironment() == -3) {
                        ToastUtils.showLong(R.string.need_permission)
                    } else {
                        findNavController().navigate(R.id.settingFragment)
                    }
                }
                storeNav.setOnClickListener {
                    if (CheckEnvironment() == -3) {
                        ToastUtils.showLong(R.string.need_permission)
                    } else {
                        findNavController().navigate(R.id.storeFragment)
                    }
                }
                forumNav.setOnClickListener {
                    if (CheckEnvironment() == -3) {
                        ToastUtils.showLong(R.string.need_permission)
                    } else {
                        findNavController().navigate(R.id.forumFragment)
                    }
                }
                aboutNav.setOnClickListener {
                    if (CheckEnvironment() == -3) {
                        ToastUtils.showLong(R.string.need_permission)
                    } else {
                        findNavController().navigate(R.id.about_nav)
                    }
                }
                navigationBar.check(R.id.beacon_nav)
            }

            fab.setOnClickListener { recycler.scrollToPosition(0) }

            header.headerLeft.setOnClickListener {
                // 按信号强弱排序
                beaconList.sortByDescending { it.device.rssi }
                deviceAdapter.notifyItemRangeChanged(0, beaconList.size)
            }

            header.headerRight.setOnClickListener {
                // 跳转到过滤界面
                startActivity(Intent(requireActivity(), FilterActivity::class.java).apply {
                    putExtra("device_type", DeviceType.BEACON)
                })
            }

            refreshableView.setOnRefreshListener {
                // 执行刷新操作
                handleRefresh()
            }

            broadcastStatus.setOnClickListener {
                // 切换展开/折叠状态
                toggleBroadcastStatus()
            }


            txtBtn.setOnClickListener {

                if (CheckEnvironment() == -3) {
                    //权限未给足
                    CheckPermissions()
                } else if (CheckEnvironment() == -1) {
                    //GPS未打开
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else if (CheckEnvironment() == -2) {
                    //蓝牙未打开
                    enableBluetooth(requireActivity())
                }

            }
        }
    }


    private val mHandler = Handler(Looper.getMainLooper())

    // 处理刷新操作
    private fun handleRefresh() {

        if (!BluetoothRepository.isBluetoothEnabled()) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
            binding?.refreshableView?.closeHeaderOrFooter()
            return
        }

        if (!isLocationEnabled(requireContext())) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2)
            binding?.refreshableView?.closeHeaderOrFooter()
            return
        }

        if (CheckEnvironment() == -3) {
            ToastUtils.showLong(R.string.need_permission)
            return
        }


        beaconList.clear()
        BluetoothRepository.stopScan()
        deviceAdapter.notifyDataSetChanged()



        mHandler.postDelayed({
            if (isVisible) {
                BluetoothRepository.startScan()
                binding?.refreshableView?.closeHeaderOrFooter()
            }

        }, 500)


    }

    override fun onPause() {
        super.onPause()
        BluetoothRepository.stopScan()
        binding?.refreshableView?.closeHeaderOrFooter()
    }

    // 切换展开/折叠状态
    private fun toggleBroadcastStatus() {
        binding?.apply {
            val (isFold, statusText) = if (broadcastStatus.text == getString(R.string.fold)) {
                ShowBroadcastStatus.FOLD to R.string.expansion
            } else {
                ShowBroadcastStatus.EXPANSION to R.string.fold
            }
            deviceAdapter.isFold = isFold
            beaconList.forEach { it.foldStatus = isFold }
            broadcastStatus.text = getString(statusText)
            deviceAdapter.notifyItemRangeChanged(0, beaconList.size, " ")
        }
    }


    override fun onBeacon(device: BluetoothDeviceWrapper) {
        // 处理检测到的 Beacon
        handleDetectedBeacon(device)
    }

    // 处理检测到的 Beacon
    private fun handleDetectedBeacon(device: BluetoothDeviceWrapper) {
        if (device.rssi <= filterBeaconRssi || (filterBeaconName.isNotEmpty() && (device.name == null || !device.name.uppercase()
                .contains(filterBeaconName.uppercase())))
        ) {
            return
        }

        requireActivity().runOnUiThread {
            val beacon = Beacon(device, 0)
            val index = beaconList.indexOf(beacon)

            if (index == -1) {
                // 新增 Beacon
                beaconList.add(beacon)
                deviceAdapter.notifyItemChanged(beaconList.size)
            } else {
                // 更新已存在的 Beacon
                val existingBeacon = beaconList[index]
                existingBeacon.count++
                val timeDifference = device.timestampNanos - existingBeacon.lastTimestamp

                if (existingBeacon.lastTimestamp != 0L) {
                    existingBeacon.updateInterval(timeDifference)
                }
                existingBeacon.lastTimestamp = device.timestampNanos
                existingBeacon.device = device

                deviceAdapter.notifyItemChanged(index, " ")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 获取过滤器中的设置
        filterBeaconName = App.sContext.getStr("device_${DeviceType.BEACON}_name")
        filterBeaconRssi = App.sContext.getInt("device_${DeviceType.BEACON}_rssi", -100)
        binding?.foot?.navigationBar?.check(R.id.beacon_nav)


        when (CheckEnvironment()) {
            0 -> {
                //权限 GPS  蓝牙都已经通过
                binding?.relaTips?.visibility = View.GONE
                binding?.refreshableView?.autoRefresh()
            }
            -1 -> {
                //GPS未打开
                binding?.relaTips?.visibility = View.VISIBLE
                binding?.txtTips?.setText(getString(R.string.tips1))
                binding?.txtBtn?.setText(getString(R.string.enable))
            }
            -2 -> {
                //蓝牙未打开
                binding?.relaTips?.visibility = View.VISIBLE
                binding?.txtTips?.setText(getString(R.string.tips2))
                binding?.txtBtn?.setText(getString(R.string.enable))
            }
            -3 -> {
                //权限未给足
                binding?.relaTips?.visibility = View.VISIBLE
                binding?.txtTips?.setText(getString(R.string.tips1))
                binding?.txtBtn?.setText(getString(R.string.enable))
            }
        }

    }


    private fun CheckEnvironment(): Int {
        if (!CheckPermissions(requireActivity())) {
            binding?.relaTips?.visibility = View.VISIBLE
            return -3
        }

        if (!isGPSEnabled(requireActivity())) {
            binding?.relaTips?.visibility = View.VISIBLE
            return -1
        }

        if (!isBluetoothEnabled()) {
            binding?.relaTips?.visibility = View.VISIBLE
            return -2
        }

        binding?.relaTips?.visibility = View.GONE
        return 0
    }


    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }


    @SuppressLint("MissingPermission")
    private fun enableBluetooth(context: Context) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableIntent)
            }
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


    private fun CheckPermissions() {
        XXPermissions.with(requireActivity())
            .permissions(getiPermissionList())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: MutableList<IPermission?>,
                    deniedList: MutableList<IPermission?>
                ) {
                    val allGranted = deniedList.isEmpty()
                    if (allGranted) {
                        binding?.refreshableView?.autoRefresh()
                    } else {
                        val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(
                            requireActivity(),
                            deniedList
                        )
                        if (doNotAskAgain) {
                            AlertDialog.Builder(requireActivity())
                                .setMessage(getString(R.string.tips3))
                                .setPositiveButton(
                                    getString(R.string.setting),
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {
                                            val intent =
                                                IntentUtils.getLaunchAppDetailsSettingsIntent("com.glowbird.gbeacon")
                                            startActivity(intent)
                                            dialog.dismiss()
                                        }
                                    }).setNegativeButton(
                                    getString(R.string.cancel),
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {
                                            dialog.dismiss()
                                        }
                                    }).show()
                        }
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}