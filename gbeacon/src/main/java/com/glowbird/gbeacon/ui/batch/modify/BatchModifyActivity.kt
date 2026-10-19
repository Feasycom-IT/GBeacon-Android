package com.glowbird.gbeacon.ui.batch.modify

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.IntentUtils
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.bean.CommandBean
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityBatchModifyBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.logic.model.Order
import com.glowbird.gbeacon.logic.utils.writeToExcel

import com.glowbird.gbeacon.ui.adapter.SettingAdapter
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.batch.scan.BatchScanActivity
import com.glowbird.gbeacon.ui.dialog.BatchModifyFinishDialog
import com.glowbird.gbeacon.ui.dialog.StopBatchModifyDialog
import com.glowbird.gbeacon.ui.dialog.TipsDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class BatchModifyActivity : BaseActivity<ActivityBatchModifyBinding>(), FscBleCallback {

    // 未配置设备列表
    private val mNotConfigDevices = mutableListOf<BluetoothDeviceWrapper>()

    // 已配置设备列表
    private val mConfigDevices = mutableListOf<BluetoothDeviceWrapper>()

    // 记录修改状态的映射
    private val mModifyMap = LinkedHashMap<String, Boolean>()

    // 适配器
    private lateinit var mNotConfigDeviceAdapter: SettingAdapter
    private lateinit var mConfigDeviceAdapter: SettingAdapter

    // 当前处理的设备
    private lateinit var mDevice: BluetoothDeviceWrapper

    // 配置失败的设备列表
    private val mConfigFailDevices = mutableListOf<BluetoothDeviceWrapper>()

    // 订单列表
    private val mOrders = mutableListOf<Order>()

    // 是否停止配置的标志
    private var isStop = false

    // 对话框
    private var mStopBatchModifyDialog: StopBatchModifyDialog? = null

    override fun getViewBinding() = ActivityBatchModifyBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        mDevice = mNotConfigDevices.firstOrNull() ?: BluetoothDeviceWrapper("") // 避免空指针异常
        if (BluetoothRepository.isConnect()) {
            BluetoothRepository.disconnect()
        } else {
            connectToDevice()
        }
    }

    override fun initView() {
        // 设置标题和按钮文本
        binding.header.headerTitle.text = getString(R.string.configuration_device)
        binding.header.headerRight.text = getString(R.string.stop)

        // 初始化适配器和RecyclerView
        mNotConfigDeviceAdapter = SettingAdapter(mNotConfigDevices)
        mConfigDeviceAdapter = SettingAdapter(mConfigDevices)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = mNotConfigDeviceAdapter

        // 添加设备到未配置设备列表，并更新UI
        mNotConfigDevices.addAll(BluetoothRepository.mDevices)
        binding.notConfigRadio.text =
            getString(R.string.not_configured_radio, mNotConfigDevices.size)
        binding.configRadio.text = getString(R.string.configured_radio, mConfigDevices.size)
    }

    override fun initEvent() {
        // 点击右上角按钮事件处理
        binding.header.headerRight.setOnClickListener {
            it as TextView
            when (it.text) {
                getString(R.string.stop) -> {
                    // 弹出停止配置对话框
                    showStopBatchModifyDialog()
                }

                getString(R.string.generate_report) -> {
                    // 生成报告
                    generateReport()
                }
            }
        }

        // RadioGroup选项改变事件处理
        binding.radioGroup.setOnCheckedChangeListener { _, p1 ->
            when (p1) {
                R.id.not_config_radio -> {
                    binding.recycler.adapter = mNotConfigDeviceAdapter
                }

                R.id.config_radio -> {
                    binding.recycler.adapter = mConfigDeviceAdapter
                }
            }
        }

        // 点击左上角返回按钮事件处理
        binding.header.headerLeft.setOnClickListener {
            navigateToBatchScanActivity()
        }
    }

    // 连接进度更新的回调
    override fun onConnectProgressUpdate(status: Int) {
        when (status) {
            CommandBean.PASSWORD_CHECK -> {
                // 校验密码
            }

            CommandBean.PASSWORD_SUCCESSFUL -> {
                // 密码正确，设置批量Beacon信息
                BluetoothRepository.setBatchBeaconInfo()
            }

            CommandBean.PASSWORD_FAILED, CommandBean.PASSWORD_TIME_OUT -> {
                // 密码错误或超时，记录失败设备
                handlePasswordFailed()
            }
        }
    }

    // AT指令回调
    @SuppressLint("NewApi")
    override fun onAtCommandCallBack(command: String?, param: String?, status: String?) {
        super.onAtCommandCallBack(command, param, status)
        when (status) {
            CommandBean.COMMAND_FAILED -> {
                // 处理AT命令失败
                handleCommandFailed(command)
            }

            CommandBean.COMMAND_SUCCESSFUL -> {
                // 处理AT命令成功
                handleCommandSuccessful(command)
            }

            CommandBean.COMMAND_FINISH -> {
                // 处理AT命令完成
                handleCommandFinish()
            }

            CommandBean.COMMAND_TIME_OUT -> {
                // 处理AT命令超时
                handleCommandTimeout(command)
            }
        }
    }

    // 设备断开连接回调
    override fun onDisconnect() {
        if (isStop) {
            // 停止配置，处理相关逻辑
            handleStopConfiguration()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            if (mNotConfigDevices.isNotEmpty()) {
                for ((x, y) in mNotConfigDevices.withIndex()) {
                    if (mConfigFailDevices.contains(y)) {
                        // 如果设备配置失败，跳过
                    } else {
                        // 继续配置下一个设备
                        mDevice = y
                        connectToDevice()
                        return@launch
                    }
                }
                // 配置完成
                batchModifyFinish()
            } else {
                // 所有设备配置完成
                batchModifyFinish()
            }
        }
    }

    // 连接设备的方法
    private fun connectToDevice() {
        BluetoothRepository.connect(mDevice, BluetoothRepository.mConnectPin)
    }

    // 停止配置对话框
    private fun showStopBatchModifyDialog() {
        mStopBatchModifyDialog = StopBatchModifyDialog(this).apply {
            mCancelOnClickListener = {
                isStop = true
                if (BluetoothRepository.isConnect()) {
                    BluetoothRepository.disconnect()
                } else {
                    binding.header.headerLeft.text = getString(R.string.back)
                    binding.header.headerRight.text = getString(R.string.generate_report)
                }
            }
            mConfirmOnClickListener = {}
            show()
        }
    }

    // 生成报告
    private fun generateReport() {


        XXPermissions.with(this@BatchModifyActivity)
            .permissions(getiPermissionList())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: MutableList<IPermission?>,
                    deniedList: MutableList<IPermission?>
                ) {

                    val allGranted = deniedList.isEmpty()
                    if (allGranted) {
                        val writeToExcel =
                            writeToExcel(this@BatchModifyActivity, BluetoothRepository.selectedDeviceMap, mOrders, "TIME")
                        val tipsDialog = TipsDialog(this@BatchModifyActivity)
                        tipsDialog.setTitle(getString(R.string.batch_modify_tips_title))
                        tipsDialog.setInfo("${getString(R.string.batch_modify_tips_title)} $writeToExcel")
                        tipsDialog.show()
                    } else {

                        val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(
                            this@BatchModifyActivity,
                            deniedList
                        )

                        if (doNotAskAgain) {
                            AlertDialog.Builder(this@BatchModifyActivity)
                                .setMessage(getString(R.string.tips4))
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


    private fun getiPermissionList(): MutableList<IPermission?> {

        val iPermissionList: MutableList<IPermission?> = ArrayList<IPermission?>()
        iPermissionList.add(PermissionLists.getBluetoothAdvertisePermission())
        iPermissionList.add(PermissionLists.getBluetoothConnectPermission())
        iPermissionList.add(PermissionLists.getBluetoothScanPermission())
        iPermissionList.add(PermissionLists.getAccessFineLocationPermission())

        return iPermissionList
    }

    // 密码错误或超时，处理失败设备
    private fun handlePasswordFailed() {
        if (!mConfigFailDevices.contains(mDevice)) {
            mConfigFailDevices.add(mDevice)
            val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())
            val format = simpleDateFormat.format(Date().time)
            mOrders.add(Order(format, mDevice.name, mDevice.address, false, null))
        }
    }

    // 处理AT命令失败
    private fun handleCommandFailed(command: String?) {
        command?.let {
            mModifyMap[it.split("AT+")[1].split("=")[0]] = false
        }
    }

    // 处理AT命令成功
    private fun handleCommandSuccessful(command: String?) {
        if (command == CommandBean.COMMAND_BEGIN) {
            // do nothing
        } else {
            command?.let {
                mModifyMap[it.split("AT+")[1].split("=")[0]] = true
            }
        }
    }

    // 处理AT命令完成
    private fun handleCommandFinish() {
        mConfigDevices.add(mDevice)
        mNotConfigDevices.remove(mDevice)
        lifecycleScope.launch(Dispatchers.Main) {
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.not_config_radio -> {
                    binding.recycler.adapter?.notifyItemRemoved(0)
                }

                R.id.config_radio -> {
                    binding.recycler.adapter?.notifyItemInserted(mConfigDevices.size)
                }
            }
            binding.notConfigRadio.text =
                getString(R.string.not_configured_radio, mNotConfigDevices.size)
            binding.configRadio.text = getString(R.string.configured_radio, mConfigDevices.size)
        }
        val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())
        val format = simpleDateFormat.format(Date().time)
        mOrders.add(Order(format, mDevice.name, mDevice.address, true, mModifyMap))
        BluetoothRepository.disconnect()
    }

    // 处理AT命令超时
    private fun handleCommandTimeout(command: String?) {
        command?.let {
            mModifyMap[it.split("AT+")[1].split("=")[0]] = false
        }
    }

    // 停止配置，处理相关逻辑
    private fun handleStopConfiguration() {
        binding.header.headerLeft.text = getString(R.string.back)
        binding.header.headerRight.text = getString(R.string.generate_report)
    }

    // 配置完成，显示对话框
    private fun batchModifyFinish() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.header.headerRight.text = ""
            BatchModifyFinishDialog(this@BatchModifyActivity).apply {
                mCancelOnClickListener = {
                    navigateToBatchScanActivity()
                }
                mConfirmOnClickListener = {
                    binding.header.headerLeft.text = getString(R.string.back)
                    binding.header.headerRight.text = getString(R.string.generate_report)
                }
                mStopBatchModifyDialog?.let {
                    try {
                        if (it.isShowing) {
                            it.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                show()
            }
        }
    }

    // 导航到批量扫描界面
    private fun navigateToBatchScanActivity() {
        Intent(this, BatchScanActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}
