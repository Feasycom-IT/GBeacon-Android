package com.glowbird.gbeacon.ui.parameter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.feasycn.bean.BeaconBean
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.bean.CommandBean
import com.glowbird.gbeacon.App.Companion.mBeaconItemDao
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityParmeterBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.logic.model.DeviceInfo
import com.glowbird.gbeacon.logic.model.Devices
import com.glowbird.gbeacon.logic.utils.ViewUtil
import com.glowbird.gbeacon.ui.adapter.SettingBeaconParameterAdapter
import com.glowbird.gbeacon.ui.addbeacon.AddBeaconActivity
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.batch.config.BatchConfigActivity
import com.glowbird.gbeacon.ui.dialog.InfoDialog
import com.glowbird.gbeacon.ui.dialog.InfoDialog.Companion.INFO_DIALOG_SHOW_TIME
import com.glowbird.gbeacon.ui.ota.OtaActivity
import com.glowbird.gbeacon.ui.utils.assetsToJsonString
import com.glowbird.gbeacon.ui.utils.isActivityTop
import com.glowbird.gbeacon.ui.view.LabelSpinnerCallBack
import com.feasycn.util.ToastUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import kotlin.collections.ArrayList

class ParameterSettingActivity : BaseActivity<ActivityParmeterBinding>(), FscBleCallback {

    // 是否返回标志
    private var mBack = false

    // 是否中断标志
    private var isInterrupt = true

    // BeaconBean 实例列表
    private val mBeaconBeans = ArrayList<BeaconBean>()

    // 懒加载 ViewModel
    private val mViewModel by lazy {
        ViewModelProviders.of(this)[ParameterSettingViewModel::class.java]
    }

    // 懒加载 BluetoothDeviceWrapper 实例
    private val mDevice: BluetoothDeviceWrapper by lazy { intent.getSerializableExtra("device") as BluetoothDeviceWrapper }

    // 懒加载设备 PIN
    private val mDevicePin: String by lazy { intent.getStringExtra("pin") ?: "000000" }

    // 检查连接的定时任务
/*    private var mCheckConnect = Runnable {
        if (!BluetoothRepository.isConnect() && (getString(R.string.connecting) == mConnectDialog.getInfo() || getString(
                R.string.check_password
            ) == mConnectDialog.getInfo())
        ) {
            mConnectDialog.setInfo(getString(R.string.timeout))
            isInterrupt = false
            // 断开连接
            BluetoothRepository.disconnect()
            mHandler.postDelayed({
                // 如果连接对话框正在显示，则关闭对话框
                if (mConnectDialog.isShowing) {
                    mConnectDialog.dismiss()
                }
                // 关闭当前页面
                finish()
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
    }*/

    // 连接对话框
    private lateinit var mConnectDialog: InfoDialog

    // 连接开始时间
    private var mConnectStartTime: Long = 0L

    // 设备信息
    private var mDeviceInfo: DeviceInfo? = null

    // IntervalSpinnerAdapter 实例
    private lateinit var mIntervalSpinnerAdapter: ArrayAdapter<String>

    // TX 功率列表
    private var mTxPowerList: List<String> = mutableListOf()



    private lateinit var mAdapter: SettingBeaconParameterAdapter

    private val mDeviceInfoMap = mutableMapOf<String, Any>()

    private val mHandler = Handler(Looper.getMainLooper())

    override fun getViewBinding() = ActivityParmeterBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注册 ViewModel 的回调
        mViewModel.registerViewCallback(this)

        // 初始化连接对话框
        mConnectDialog = InfoDialog(this, "")

        // 如果未连接
        if (!mViewModel.isConnect()) {
            var flag = false

            // 如果设备 PIN 码长度不为 6
            if (mDevicePin.length != 6) {
                flag = true
                // 弹出提示框：PIN 码错误
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.pin_error_info))
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener { finish() }
                    .show()
            }

            // 如果蓝牙未开启
            if (!flag && !BluetoothRepository.isBluetoothEnabled()) {
                flag = true
                // 弹出提示框：蓝牙未开启
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bt_enabled))
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener { finish() }
                    .show()
            }

            // 如果未出现错误
            if (!flag) {
                // 显示连接对话框
                mConnectDialog.setInfo(getString(R.string.connecting))
                mConnectDialog.show()

                // 进行连接
                mViewModel.connect(mDevice, mDevicePin)
                BluetoothRepository.mConnectPin = mDevicePin
            }
        }
    }

    override fun initView() {
        // 记录连接开始时间
        mConnectStartTime = System.currentTimeMillis()

        // 设置标题为设备名称或未知名称
        binding.header.headerTitle.text =
            mDevice.completeLocalName ?: mDevice.name ?: getString(R.string.unknown_name)

        // 如果设备名称包含特定字符，则隐藏相应的设置项
//        if (mDevice.name.contains("104EA") || mDevice.name.contains("104EL")) {
//            binding.intervalLabelEdit.visibility = View.GONE
//            binding.txPowerLabelSpinner.visibility = View.GONE
//        }

        // 设置标题栏右侧文本为“保存”，左侧文本为“返回设置”
        binding.header.headerRight.text = getString(R.string.save)
        binding.header.headerLeft.text = getString(R.string.to_setting)

        // 初始化适配器并设置 RecyclerView 布局管理器和适配器
        mAdapter = SettingBeaconParameterAdapter(this, mBeaconBeans)
        with(binding.parameterListview) {
            layoutManager = LinearLayoutManager(this@ParameterSettingActivity)
            adapter = mAdapter
        }

        // 异步获取设备信息
        lifecycleScope.launch(Dispatchers.IO) {
            mDeviceInfo =
                mBeaconItemDao.queryDeviceByNumber(mDevice.feasyBeacon.module.toInt()) ?: run {
                    // 如果数据库中未找到设备信息，则从 JSON 文件中加载
                    val json = assetsToJsonString("Device.json")
                    val gson = Gson()
                    val devices = gson.fromJson(json, Devices::class.java)
                    devices?.data?.list?.find { it.number == mDevice.feasyBeacon.module.toInt() }
                }
            // 更新 UI
            mDeviceInfo?.let { deviceInfo ->
                withContext(Dispatchers.Main) {
                    // 设置模块名称
                    binding.moduleLabelEdit.setText(deviceInfo.name, false)

                    // 设置默认间隔
                    if (deviceInfo.defaultInterval.isNotEmpty() && "~" in deviceInfo.defaultInterval) {
                        binding.intervalLabelEdit.setRange(
                            deviceInfo.defaultInterval.split("~").toTypedArray()
                        )
                    }

                    // 设置 G-Sensor 间隔
                    if (deviceInfo.gsensorInterval.isNotEmpty()) {
                        binding.gsensorEdit.setRange(deviceInfo)
                    }

                    // 设置按键配置间隔
                    if (deviceInfo.keyInterval.isNotEmpty()) {
                        binding.keycfgEdit.setRange(deviceInfo)
                    }
                }
            }
        }

        // 添加文本监听器
        binding.nameLabelEdit.setTextWatcher(ViewUtil.NameTextWatcher(binding.nameLabelEdit))
        binding.pinLabelEdit.setTextWatcher(ViewUtil.PinTextWatcher(binding.pinLabelEdit))
    }

    override fun initEvent() {
        // 点击标题栏左侧按钮
        binding.header.headerLeft.setOnClickListener {
            // 断开连接
            BluetoothRepository.disconnect()
            // 标记返回
            mBack = true
        }

        // 点击标题栏右侧按钮（保存）
        binding.header.headerRight.setOnClickListener {
            // 隐藏键盘
            hideInput()

            // 检查设备名称是否为空
            if (!binding.nameLabelEdit.check()) {
                showDialogInterval(getString(R.string.name_null_info))
                return@setOnClickListener
            }

            // 检查 PIN 码是否正确
            if (!binding.pinLabelEdit.check()) {
                showDialogInterval(getString(R.string.pin_error_info))
                return@setOnClickListener
            }

            try {
                if (binding.intervalLabelEdit.interval < binding.intervalLabelEdit.intervalMin && binding.intervalLabelEdit.interval != 0) {
                    binding.intervalLabelEdit.setInterval(binding.intervalLabelEdit.intervalMin.toString())
                    showDialog(
                        getString(
                            R.string.interval_advin_info,
                            binding.intervalLabelEdit.intervalMin,
                            binding.intervalLabelEdit.intervalMax
                        )
                    )
                    return@setOnClickListener
                } else if (binding.intervalLabelEdit.interval > binding.intervalLabelEdit.intervalMax) {
                    binding.intervalLabelEdit.setInterval(binding.intervalLabelEdit.intervalMax.toString())
                    showDialog(
                        getString(
                            R.string.interval_advin_info,
                            binding.intervalLabelEdit.intervalMin,
                            binding.intervalLabelEdit.intervalMax
                        )
                    )
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                showDialog(getString(R.string.interval_not_null))
                return@setOnClickListener
            }

            try{
                if (binding.gsensorEdit.visibility != View.GONE) {
                    if (binding.gsensorEdit.getAdvin() < binding.gsensorEdit.advinMin && binding.gsensorEdit.getAdvin() != 0) {
                        binding.gsensorEdit.setAdvin(binding.gsensorEdit.advinMin)
                        showDialog(
                            getString(
                                R.string.gsensor_advin_info,
                                binding.gsensorEdit.advinMin,
                                binding.gsensorEdit.advinMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getAdvin() > binding.gsensorEdit.advinMax) {
                        binding.gsensorEdit.setAdvin(binding.gsensorEdit.advinMax)
                        showDialog(
                            getString(
                                R.string.gsensor_advin_info,
                                binding.gsensorEdit.advinMin,
                                binding.gsensorEdit.advinMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getDuration() < binding.gsensorEdit.durationMin) {
                        binding.gsensorEdit.setDuration(binding.gsensorEdit.durationMin)
                        showDialog(
                            getString(
                                R.string.gsensor_duration_info,
                                binding.gsensorEdit.durationMin,
                                binding.gsensorEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getDuration() > binding.gsensorEdit.durationMax) {
                        binding.gsensorEdit.setDuration(binding.gsensorEdit.durationMax)
                        showDialog(
                            getString(
                                R.string.gsensor_duration_info,
                                binding.gsensorEdit.durationMin,
                                binding.gsensorEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                }
            } catch (e: Exception) {
                showDialog(getString(R.string.gsensor_interval_not_null))
                return@setOnClickListener
            }

            try{
                if (binding.keycfgEdit.visibility != View.GONE) {
                    if (binding.keycfgEdit.getInterval() < binding.keycfgEdit.intervalMin && binding.keycfgEdit.getInterval() != 0) {
                        binding.keycfgEdit.setInterval(binding.keycfgEdit.intervalMin.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_advin_info,
                                binding.keycfgEdit.intervalMin,
                                binding.keycfgEdit.intervalMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getInterval() > binding.keycfgEdit.intervalMax) {
                        binding.keycfgEdit.setInterval(binding.keycfgEdit.intervalMax.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_advin_info,
                                binding.keycfgEdit.intervalMin,
                                binding.keycfgEdit.intervalMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getDuration() < binding.keycfgEdit.durationMin) {
                        binding.keycfgEdit.setDuration(binding.keycfgEdit.durationMin.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_duration_info,
                                binding.keycfgEdit.durationMin,
                                binding.keycfgEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getDuration() > binding.keycfgEdit.durationMax) {
                        binding.keycfgEdit.setDuration(binding.keycfgEdit.durationMax.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_duration_info,
                                binding.keycfgEdit.durationMin,
                                binding.keycfgEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                }
            }catch (e: Exception) {
                showDialog(getString(R.string.key_interval_not_null))
                return@setOnClickListener
            }

            // 检查设置是否通过验证
            if (verify()) {
                // 显示保存状态
                mConnectDialog.setInfo(getString(R.string.status_save))
                mConnectDialog.show()
                // 保存设置
                BluetoothRepository.setBeaconInfo()
            } else {
                // 显示间隔信息提示
                showDialogInterval(getString(R.string.interval_message_4))
            }
        }

        // 点击“添加”按钮
        binding.addButton.setOnClickListener {
            // 跳转到添加设备页面
            val intent = Intent(this, AddBeaconActivity::class.java)
            intent.putExtra("dev_", mDevice)
            startActivity.launch(intent)
        }

        // 点击“OTA”按钮
        binding.otaButton.setOnClickListener {
            // 跳转到OTA页面
            val intent = Intent(this, OtaActivity::class.java)
            intent.putExtra("device", mDevice)
            intent.putExtra("pin", mDevicePin)
            startActivity(intent)
            // 标记返回
            mBack = true
        }

        // 点击“批量配置”按钮
        binding.batchButton.setOnClickListener {
            Intent(this, BatchConfigActivity::class.java).apply {
                // 传递设备信息到批量配置页面
                val mBundle = Bundle()
                mBundle.putSerializable("device_info_map", mDeviceInfoMap as Serializable)
                mBundle.putSerializable("device_info", mDeviceInfo as Serializable)
                putExtras(mBundle)
                startActivity(this)
            }
        }

        // 点击“恢复出厂设置”按钮
        binding.resettingButton.setOnClickListener {
            // 弹出确认恢复对话框
            AlertDialog.Builder(this).setMessage(resources.getString(R.string.reset_the_device))
                .setPositiveButton(
                    resources.getString(R.string.reset)
                ) { _, _ ->
                    // 显示恢复状态
                    mConnectDialog.setInfo(getString(R.string.status_resting))
                    mConnectDialog.show()
                    // 恢复出厂设置
                    BluetoothRepository.restore()
                }.setNegativeButton(resources.getString(R.string.cancel), null).show()
        }

        // AT指令调试
        binding.atCommandDebuggingButton.setOnClickListener {
            // 跳转到指令设置页面
            val intent = Intent(this, AtCommandDebugging::class.java)
            startActivity(intent)
            // 标记返回
            mBack = true
        }
    }

    private fun verify(): Boolean {
        // 返回 true 表示验证通过
        // TODO 给TrueSpot客户定制最大间隔时长12秒 modify by cjg: 2023-07-27
        // return if (binding.intervalLabelEdit.interval <= 13000 && binding.intervalLabelEdit.interval != 0) {
        // TODO 这里是公板代码
        return if (binding.intervalLabelEdit.interval <= 10000 && binding.intervalLabelEdit.interval != 0) {
            true
        } else if (binding.gsensorEdit.visibility != View.GONE && (binding.gsensorEdit.getAdvin() <= 10000 && binding.gsensorEdit.getAdvin() != 0)) {
            true
        } else binding.keycfgEdit.visibility != View.GONE && (binding.keycfgEdit.getInterval() <= 10000 && binding.keycfgEdit.getInterval() != 0)
    }

    private val startActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //此处是跳转的result回调方法
            if (it.resultCode == AddBeaconActivity.REQUEST_BEACON_ADD_OK) {
                val beaconBean = it.data?.getSerializableExtra("beaconBean") as BeaconBean?
                beaconBean?.let {
                    BluetoothRepository.addBeaconInfo(beaconBean)
                    beaconBean.index = (mBeaconBeans.size + 1).toString()
                    mBeaconBeans.add(beaconBean)
                    addBeaconEnable(!BluetoothRepository.isBeaconInfoFull())
                    mAdapter.notifyItemChanged(mBeaconBeans.size)
                }
            }
        }

    override fun onConnectProgressUpdate(status: Int) {
        Log.e(TAG,"onConnectProgressUpdate======== => $status")
        super.onConnectProgressUpdate(status)
        // 根据连接状态更新界面提示信息
        when (status) {
            CommandBean.PASSWORD_CHECK -> {
                Log.e(TAG,"PASSWORD_CHECK")
                // 正在检查密码
                mConnectDialog.setInfo(getString(R.string.check_password))
            }
            CommandBean.PASSWORD_SUCCESSFUL -> {
                Log.e(TAG,"PASSWORD_SUCCESSFUL")
                // 密码验证成功
                mConnectDialog.setInfo(getString(R.string.password_success))
                // 获取设备信息
                mViewModel.getDeviceInfo(mDevice.feasyBeacon, true)
            }
            CommandBean.PASSWORD_FAILED -> {
                // 密码验证失败
                mConnectDialog.setInfo(getString(R.string.password_failed))
                // 标记不中断连接
                isInterrupt = false
            }
            CommandBean.PASSWORD_TIME_OUT -> {
                // 连接超时
                mConnectDialog.setInfo(getString(R.string.timeout))
                // 标记不中断连接
                isInterrupt = false
                // 断开连接
                BluetoothRepository.disconnect()
                mHandler.postDelayed({
                    // 如果连接对话框正在显示，则关闭对话框
                    if (mConnectDialog.isShowing) {
                        mConnectDialog.dismiss()
                    }
                    // 关闭当前页面
                    finish()
                }, INFO_DIALOG_SHOW_TIME.toLong())
            }
        }
    }

    override fun onDeviceInfo(parameterName: String?, parameter: Any?) {
//        Log.e(TAG, "parameterName  ->  $parameterName   parameter  ->  $parameter")
        // 如果参数名和参数都不为空，则将参数添加到设备信息映射中
        if (parameterName != null && parameter != null) {
            mDeviceInfoMap[parameterName] = parameter
        }
        // 在主线程中处理设备信息
        lifecycleScope.launch(Dispatchers.Main) {
            when (parameterName) {
                CommandBean.COMMAND_BEGIN -> {
                    // 开始连接
                    mConnectDialog.setInfo(getString(R.string.status_connected))
                }
                CommandBean.COMMAND_VERSION -> {
                    // 版本信息
                    binding.versionLabelEdit.setText(parameter as String, false)
                }
                CommandBean.COMMAND_ADVIN -> {
                    // 广播间隔
                    var intervalTime = parameter as String
                    binding.intervalLabelEdit.setInterval(intervalTime)
                }
                CommandBean.COMMAND_KEYCFG -> {
                    // 按键配置
                    val s = parameter as Array<String>
                    binding.keyGroup.visibility = View.VISIBLE
                    binding.keycfgEdit.setInterval(s[0])
                    binding.keycfgEdit.setDuration(s[1])
                }
                CommandBean.COMMAND_GSCFG -> {
                    // 重力传感器配置
                    binding.gsensorGroup.visibility = View.VISIBLE
                    val s = parameter as Array<*>
                    binding.gsensorEdit.setAdvin((s[0] as String).toInt())
                    binding.gsensorEdit.setDuration((s[1] as String).toInt())
                    if (s.size == 3) {
                        binding.gsensorEdit.setSensitivityEdit((s[2] as String).toInt())
                    }
                }
                CommandBean.COMMAND_BUZ, CommandBean.COMMAND_LED -> {
                    // 蜂鸣器和LED灯配置
                    // 不做处理
                }
                CommandBean.COMMAND_BWMODE -> {
                    // 蓝牙模式
                    // 不做处理
                }
                CommandBean.COMMAND_PIN -> {
                    // PIN码
                    val temp = parameter as String
                    binding.pinLabelEdit.setText(
                        when (temp.length) {
                            in 1..5 -> "0".repeat(5 - temp.length) + temp
                            else -> temp
                        },
                        true
                    )
                }
                CommandBean.COMMAND_TX_POWER -> {
                    // 发射功率
                    try {
                        // 将发射功率列表设置到下拉框适配器中
                        mTxPowerList = mDeviceInfo?.txPower?.split(",") ?: emptyList()
                        mIntervalSpinnerAdapter = ArrayAdapter<String>(
                            this@ParameterSettingActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            mTxPowerList
                        )
                        binding.txPowerLabelSpinner.spinnerInit(
                            mIntervalSpinnerAdapter,
                            mDeviceInfo!!.name,
                            object : LabelSpinnerCallBack {
                                override fun selectCallback(p: Int) {
                                    // 下拉框选中回调
                                }
                            }
                        )

                        val hexPosition = parameter as String
                        Log.e(TAG, "hexPosition => $hexPosition")
                        binding.txPowerLabelSpinner.setSelect(hexPosition.toInt())

                        // 延迟两秒后更新广播参数
                        delay(2000)
                        BluetoothRepository.beaconBroadcastParameters()
                    } catch (e: NullPointerException) {
                        Log.e(TAG, "设置发射功率空指针异常 => ${e.printStackTrace()}")
                    }
                }
                CommandBean.COMMAND_BADVDATA -> {
                    // 广播数据
                    (parameter as ArrayList<BeaconBean>).forEach { beaconBeacon ->
                        if (beaconBeacon.beaconType.isNotBlank()) {
                            mBeaconBeans.add(beaconBeacon)
                        }
                    }
                    // 更新适配器
                    if (mViewModel.isBeaconInfoFull()) {
                        addBeaconEnable(false)
                    }
                    mAdapter.notifyItemRangeInserted(0, mBeaconBeans.size)
                }
                CommandBean.COMMAND_BADVPARM -> {
                    // 广播参数
                    for (i in 0 until mBeaconBeans.size) {
                        val basdvs = (parameter as String).trim().split("BADVPARM=")

                        Log.e(
                            TAG,
                            "interval => ${basdvs[i + 1].split(",")[2].toInt()}  phy => ${
                                basdvs[i + 1].split(",")[1]
                            }  txPower => ${basdvs[i + 1].split(",")[3]}"
                        )

                        mBeaconBeans[i].interval = basdvs[i + 1].split(",")[2].toInt()
                        mBeaconBeans[i].phy = basdvs[i + 1].split(",")[1]
                        mBeaconBeans[i].txpower = basdvs[i + 1].split(",")[3]
                        if (mDeviceInfo?.name.equals("BP104EL")) {
                            mBeaconBeans[i].parentName = "BP104EL"
                        } else if (mDeviceInfo?.name.equals("BP104EA")) {
                            mBeaconBeans[i].parentName = "BP104EA"
                        } else if (mDeviceInfo?.name.equals("BP104E 2nd")) {
                            mBeaconBeans[i].parentName = "BP104E 2nd"
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
                CommandBean.COMMAND_EXTEND -> {
                    // 扩展信息
                    // 不做处理
                }
                CommandBean.COMMAND_TLM -> {
                    // TLM信息
                    binding.tlmGroup.visibility = View.VISIBLE
                    binding.tlmLabelButton.visibility = View.VISIBLE
                    binding.tlmLabelButton.setCheck(parameter == "1")
                    binding.tlmLabelButton.setOnToggleChanged { _, isChecked ->
                        BluetoothRepository.setTlm(isChecked)
                    }
                }
                CommandBean.COMMAND_ADVEXT -> {
                    // 广播扩展
                    binding.longRandGroup.visibility = View.VISIBLE
                    binding.longRangeLabelButton.visibility = View.VISIBLE
                    binding.longRangeLabelButton.setCheck(parameter == "1")
                    binding.longRangeLabelButton.setOnToggleChanged { _, isChecked ->
                        BluetoothRepository.setBroadcastExtensions(isChecked)
                    }
                }
                CommandBean.COMMAND_NAME -> {
                    // 设备名称
//                    Log.e("TAG","NAME => ${parameter as String}")
                    binding.nameLabelEdit.setText(parameter as String, true)
                }
            }
        }
    }

    override fun onAtCommandCallBack(command: String?, param: String?, status: String?) {
        // 检查命令执行状态是否为完成，并且连接状态为保存时
        Log.e("TAG","onAtCommandCallBack command => $command  param => $param  status => $status")
        if (CommandBean.COMMAND_FINISH == status && applicationContext.getString(R.string.status_save) == mConnectDialog.getInfo()) {
            // 设置信息为完成状态
            mConnectDialog.setInfo(applicationContext.getString(R.string.status_finish))
            // 断开连接
            BluetoothRepository.disconnect()
            mHandler.postDelayed({
                // 如果连接对话框正在显示，则关闭对话框
                if (mConnectDialog.isShowing) {
                    mConnectDialog.dismiss()
                }
                // 关闭当前页面
                finish()
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
        // 如果命令执行状态为超时
        if (CommandBean.COMMAND_TIME_OUT == status) {
            // 设置信息为超时状态
            mConnectDialog.setInfo(applicationContext.getString(R.string.status_timeout))
            // 断开连接
            BluetoothRepository.disconnect()
            mHandler.postDelayed({
                // 如果连接对话框正在显示，则关闭对话框
                if (mConnectDialog.isShowing) {
                    mConnectDialog.dismiss()
                }
                // 关闭当前页面
                finish()
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
        // 如果命令为重置设备
        if (command.equals("AT+RESTORE")) {
            mHandler.post(kotlin.run {
                Runnable {
                    try {
                        // 如果连接对话框正在显示，则关闭对话框
                        if (mConnectDialog.isShowing) {
                            mConnectDialog.dismiss()
                        }
                        // 如果当前页面处于栈顶，则显示重置成功的提示信息
                        if (isActivityTop(ParameterSettingActivity::class.java, this)) {
                            ToastUtil.show(this, getString(R.string.reset_success))
                            mHandler.postDelayed({
                                try {
                                    BluetoothRepository.disconnect()
                                    finish()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, INFO_DIALOG_SHOW_TIME.toLong())
                        }
                    } catch (_: Exception) {
                        // 异常捕获
                    }
                }
            })
        }
        // 如果命令执行状态为完成，并且连接状态为已连接
        if (CommandBean.COMMAND_FINISH == status && applicationContext.getString(R.string.status_connected) == mConnectDialog.getInfo()) {
            // 关闭连接对话框
            mConnectDialog.dismiss()
            // 移除检查连接的回调
            //mHandler.removeCallbacks(mCheckConnect)
        } else {
            // 处理其他状态
            when {
                CommandBean.COMMAND_TIME_OUT == status -> {
                    // 超时状态
                }
                CommandBean.COMMAND_FAILED == status -> {
                    // 失败状态
                }
                CommandBean.COMMAND_SUCCESSFUL == status -> {
                    // 成功状态
                }
                CommandBean.COMMAND_NO_NEED == status -> {
                    // 不需要状态
                }
            }
        }
    }

    override fun onDeleteBeaconInfo(beaconBean: BeaconBean) {
        mBeaconBeans.remove(beaconBean)
        mAdapter.notifyDataSetChanged()
        addBeaconEnable(true)
    }

    private fun addBeaconEnable(enable: Boolean) {
        binding.addButton.visibility = if (enable) View.VISIBLE else View.GONE
    }

    /**
     * 显示间隔错误对话框
     * @param message 错误信息
     */
    private fun showDialogInterval(message: String) {
        // 创建并显示错误对话框
        AlertDialog.Builder(this).setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    /**
     * 显示警告对话框
     * @param message 警告信息
     */
    private fun showDialog(message: String) {
        // 创建并显示警告对话框
        AlertDialog.Builder(this).setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    /**
     * 隐藏键盘
     */
    private fun hideInput() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window.peekDecorView()
        if (null != v) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    /**
     * 处理按键按下事件
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                // 用户按下返回键
                mBack = true
                BluetoothRepository.disconnect()
                finish()
            }
            else -> {
                // 其他按键事件不做处理
            }
        }
        // 返回false表示继续向下传递事件
        return false
    }

    override fun onDisconnect() {
        super.onDisconnect()
        connectFailedHandler()
    }

    /**
     * 处理连接失败的情况
     */
    private fun connectFailedHandler() {
        val failType: String?
        // 判断连接失败类型
        if (mBack) {
            // 如果是因为用户点击返回按钮，直接关闭当前界面
            finish()
        } else {
            // 如果是其他原因导致连接失败
            if (isInterrupt) {
                // 如果是因为连接被中断
                failType = resources.getString(R.string.connection_disconnected)
            } else {
                // 如果是因为密码错误
                failType = resources.getString(R.string.pin_error)
                isInterrupt = true
            }
            Log.e(TAG, "failType => $failType")
            // 延迟一段时间后关闭连接并关闭当前界面
            mHandler.postDelayed({
                try {
                    if (mConnectDialog.isShowing) {
                        mConnectDialog.dismiss()
                    }
                    BluetoothRepository.disconnect()
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unRegisterViewCallback(this)
    }

    companion object {
        private const val TAG = "ParaSetActivity"
    }

}