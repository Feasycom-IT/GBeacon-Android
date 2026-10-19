package com.glowbird.gbeacon.ui.batch.config

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Range
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.feasycn.bean.CommandBean
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityBatchConfigBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.model.DeviceInfo
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.batch.scan.BatchScanActivity
import com.glowbird.gbeacon.ui.dialog.TipsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchConfigActivity : BaseActivity<ActivityBatchConfigBinding>() {

    lateinit var mDeviceInfoMap: MutableMap<String, Any>
    lateinit var mDeviceInfo: DeviceInfo
    lateinit var mTxPowerList: List<String>

    private var mIntervalRange: Range<Int>? = null
    private var mGSensorIntervalRange: Range<Int>? = null
    private var mGSensorDurationRange: Range<Int>? = null
    private var mKeyIntervalRange: Range<Int>? = null
    private var mKeyDurationRange: Range<Int>? = null

    private val checkBoxList by lazy {
        mutableListOf(
            binding.nameCheckBox,
            binding.intervalCheckBox,
            binding.tlmCheckBox,
            binding.longRangeCheckBox,
            binding.pinCheckBox,
            binding.txPowerCheckBox,
            binding.gsensorCheckBox,
            binding.keyCheckBox
        )
    }

    override fun getViewBinding() = ActivityBatchConfigBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        mDeviceInfoMap = intent.getSerializableExtra("device_info_map") as MutableMap<String, Any>
        mDeviceInfo = intent.getSerializableExtra("device_info") as DeviceInfo
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mDeviceInfoMap.forEach { (t, u) ->
                onDeviceInfo(t, u)
            }
        }
        binding.moduleEdit.text = mDeviceInfo.name
        if (mDeviceInfo.txPower.isNotEmpty()) {
            mTxPowerList = mDeviceInfo.txPower.split(",")
            val intervalSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mTxPowerList)
            binding.txPowerSpinner.adapter = intervalSpinnerAdapter
        }
        BluetoothRepository.commandBean.command.clear()
        mDeviceInfo.let { deviceInfo ->
            if (deviceInfo.defaultInterval.isNotEmpty()) {
                val intervalRangeList = deviceInfo.defaultInterval.split("~")
                mIntervalRange = Range(intervalRangeList[0].toInt(), intervalRangeList[1].toInt())

                binding.intervalEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().isNotEmpty() && (s.toString().toInt() >= mIntervalRange!!.lower || s.toString().toInt() == 0) && s.toString().toInt() <= mIntervalRange!!.upper) {

                        } else {
                            if (s.toString().isNotEmpty() && s.toString().toInt() > mIntervalRange!!.upper) {
                                binding.intervalEdit.setText(mIntervalRange!!.upper.toString())
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }
            if (deviceInfo.gsensorInterval.isNotEmpty()) {
                val gSensorRangeList = deviceInfo.gsensorInterval.split("~")
                mGSensorIntervalRange = Range(gSensorRangeList[0].toInt(), gSensorRangeList[1].toInt())

                binding.gsensorAdvinEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().isNotEmpty() && (s.toString().toInt() >= mGSensorIntervalRange!!.lower || s.toString().toInt() == 0) && s.toString().toInt() <= mGSensorIntervalRange!!.upper) {

                        } else {
                            if (s.toString().isNotEmpty() && s.toString().toInt() > mGSensorIntervalRange!!.upper) {
                                binding.gsensorAdvinEdit.setText(mGSensorIntervalRange!!.upper.toString())
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })

                val gSensorDurationRangeList = deviceInfo.gsensorDuration.split("~")
                mGSensorDurationRange = Range(gSensorDurationRangeList[0].toInt(), gSensorDurationRangeList[1].toInt())

                binding.gsensorDurationEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence, start: Int, count: Int, after: Int
                    ) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().isNotEmpty() && (s.toString().toInt() >= mGSensorDurationRange!!.lower || s.toString().toInt() == 0) && s.toString().toInt() <= mGSensorDurationRange!!.upper
                        ) {

                        } else {
                            if (s.toString().isNotEmpty() && s.toString().toInt() > mGSensorDurationRange!!.upper) {
                                binding.gsensorDurationEdit.setText(mGSensorDurationRange!!.upper.toString())
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }
            if (deviceInfo.keyInterval.isNotEmpty()) {
                val keyIntervalRangeList = deviceInfo.gsensorInterval.split("~")
                mKeyIntervalRange = Range(keyIntervalRangeList[0].toInt(), keyIntervalRangeList[1].toInt())

                binding.keyAdvin.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().isNotEmpty() && (s.toString().toInt() >= mKeyIntervalRange!!.lower || s.toString().toInt() == 0) && s.toString().toInt() <= mKeyIntervalRange!!.upper) {

                        } else {
                            if (s.toString().isNotEmpty() && s.toString().toInt() > mKeyIntervalRange!!.upper) {
                                binding.keyAdvin.setText(mKeyIntervalRange!!.upper.toString())
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })

                val keyDurationRangeList = deviceInfo.keyDuration.split("~")
                mKeyDurationRange = Range(keyDurationRangeList[0].toInt(), keyDurationRangeList[1].toInt())

                binding.keyDuration.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().isNotEmpty() && (s.toString().toInt() >= mKeyDurationRange!!.lower || s.toString().toInt() == 0) && s.toString().toInt() <= mKeyDurationRange!!.upper) {

                        } else {
                            if (s.toString().isNotEmpty() && s.toString().toInt() > mKeyDurationRange!!.upper) {
                                binding.keyDuration.setText(mKeyDurationRange!!.upper.toString())
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }
        }
    }

    override fun initView() {
        binding.header.headerLeft.text = getString(R.string.back)
        binding.header.headerTitle.text = getString(R.string.bulk_configuration_information)
        binding.header.headerRight.text = getString(R.string.start)
    }

    override fun initEvent() {
        BluetoothRepository.selectedDeviceMap.clear()
        binding.header.headerLeft.setOnClickListener { finish() }

        binding.header.headerRight.setOnClickListener {
            checkBoxList.filter {
                it.isChecked
            }.let {
                if (it.isNotEmpty()) {
                    if (binding.intervalCheckBox.isChecked) {
                        mIntervalRange?.let {
                            val intervalInt = try {
                                binding.intervalEdit.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }
                            if (intervalInt < it.lower && intervalInt != 0) {
                                binding.intervalEdit.setText(it.lower.toString())
                                showDialog(
                                    getString(
                                        R.string.interval_advin_info, it.lower, it.upper
                                    )
                                )
                                return@setOnClickListener
                            } else if (intervalInt > it.upper) {
                                binding.intervalEdit.setText(it.upper.toString())
                                showDialog(
                                    getString(
                                        R.string.interval_advin_info, it.lower, it.upper
                                    )
                                )
                                return@setOnClickListener
                            }
                        }
                    }

                    if (binding.gsensorCheckBox.isChecked) {
                        mGSensorIntervalRange?.let { it ->
                            val gSensorAdvinInt = try {
                                binding.gsensorAdvinEdit.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }
                            if (gSensorAdvinInt < it.lower && gSensorAdvinInt != 0) {
                                binding.gsensorAdvinEdit.setText(it.lower.toString())
                                showDialog(getString(
                                    R.string.gsensor_advin_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }
                            if (gSensorAdvinInt > it.upper) {
                                binding.gsensorAdvinEdit.setText(it.upper.toString())
                                showDialog(getString(
                                    R.string.gsensor_advin_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }
                        }

                        mGSensorDurationRange?.let { it ->
                            val gSensorDurationInt = try {
                                binding.gsensorDurationEdit.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }

                            if (gSensorDurationInt < it.lower) {
                                binding.gsensorDurationEdit.setText(it.lower.toString())
                                showDialog(getString(R.string.gsensor_duration_info, it.lower, it.upper))
                                return@setOnClickListener
                            }
                            if (gSensorDurationInt > it.upper) {
                                binding.gsensorDurationEdit.setText(it.upper.toString())
                                showDialog(getString(
                                    R.string.gsensor_duration_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }
                        }
                    }

                    if (binding.keyCheckBox.isChecked) {
                        mKeyIntervalRange?.let { it ->
                            val keyAdvinInt = try {
                                binding.keyAdvin.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }
                            if (keyAdvinInt < it.lower && keyAdvinInt != 0) {
                                binding.keyAdvin.setText(it.lower.toString())
                                showDialog(getString(
                                    R.string.keycfg_advin_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }

                            if (keyAdvinInt > it.upper) {
                                binding.keyAdvin.setText(it.upper.toString())
                                showDialog(getString(
                                    R.string.keycfg_advin_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }

                        }

                        mKeyDurationRange?.let { it ->
                            val keyDurationInt = try {
                                binding.keyDuration.text.toString().toInt()
                            } catch (e: Exception) {
                                0
                            }
                            if (keyDurationInt < it.lower) {
                                binding.keyDuration.setText(it.lower.toString())
                                showDialog(getString(
                                    R.string.keycfg_duration_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }
                            if (keyDurationInt > it.upper) {
                                binding.keyDuration.setText(it.upper.toString())
                                showDialog(getString(
                                    R.string.keycfg_duration_info, it.lower, it.upper
                                ))
                                return@setOnClickListener
                            }
                        }
                    }

                    if (verify()) {
                        it.forEach {
                            BluetoothRepository.run {
                                when (it.id) {
                                    R.id.name_check_box -> {
                                        commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_NAME + "=" + mDeviceInfoMap[CommandBean.COMMAND_NAME])
                                        mDeviceInfoMap[CommandBean.COMMAND_NAME]!!.also {
                                            selectedDeviceMap[CommandBean.COMMAND_NAME] = it
                                        }

                                    }
                                    R.id.interval_check_box -> {
                                        commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_ADVIN + "=${mDeviceInfoMap[CommandBean.COMMAND_ADVIN]}")
                                        selectedDeviceMap[CommandBean.COMMAND_ADVIN] =
                                            mDeviceInfoMap[CommandBean.COMMAND_ADVIN]!!
                                    }
                                    R.id.tlm_check_box -> {
                                        commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_TLM + "=${mDeviceInfoMap[CommandBean.COMMAND_TLM]}")
                                        selectedDeviceMap[CommandBean.COMMAND_TLM] =
                                            mDeviceInfoMap[CommandBean.COMMAND_TLM]!!
                                    }
                                    R.id.long_range_check_box -> {
                                        if (mDeviceInfoMap[CommandBean.COMMAND_ADVEXT] == true) {
                                            commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_ADVEXT + "=1")
                                        } else {
                                            commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_ADVEXT + "=0")
                                        }
                                        selectedDeviceMap[CommandBean.COMMAND_ADVEXT] =
                                            mDeviceInfoMap[CommandBean.COMMAND_ADVEXT]!!
                                    }
                                    R.id.pin_check_box -> {
                                        commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_PIN + "=" + mDeviceInfoMap[CommandBean.COMMAND_PIN])
                                        selectedDeviceMap[CommandBean.COMMAND_PIN] =
                                            mDeviceInfoMap[CommandBean.COMMAND_PIN]!!
                                    }
                                    R.id.tx_power_check_box -> {
                                        commandBean.setCommand(
                                            CommandBean.COMMAND_HEADER + CommandBean.COMMAND_TX_POWER + "=" + (mDeviceInfoMap[CommandBean.COMMAND_TX_POWER] as Int).toString(
                                                16
                                            )
                                        )
                                        selectedDeviceMap[CommandBean.COMMAND_TX_POWER] =
                                            mTxPowerList[mDeviceInfoMap[CommandBean.COMMAND_TX_POWER] as Int]
                                    }
                                    R.id.gsensor_check_box -> {
                                        var strings =
                                            mDeviceInfoMap[CommandBean.COMMAND_GSCFG] as Array<String>

                                        if (strings.size == 2) {
                                            commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_GSCFG + "=" + strings[0] + "," + strings[1])
                                        } else if (strings.size == 3) {
                                            commandBean.setCommand(
                                                CommandBean.COMMAND_HEADER + CommandBean.COMMAND_GSCFG + "=" + strings[0] + "," + strings[1] + "," + strings[2]
                                            )
                                        }
                                        selectedDeviceMap[CommandBean.COMMAND_GSCFG] =
                                            mDeviceInfoMap[CommandBean.COMMAND_GSCFG]!!
                                    }
                                    R.id.key_check_box -> {
                                        commandBean.setCommand(CommandBean.COMMAND_HEADER + CommandBean.COMMAND_KEYCFG + "=" + binding.keyAdvin.text.toString() + "," + binding.keyDuration.text.toString())
                                        selectedDeviceMap[CommandBean.COMMAND_KEYCFG] =
                                            mDeviceInfoMap[CommandBean.COMMAND_KEYCFG]!!
                                    }
                                }
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            BluetoothRepository.selectedDeviceMap.forEach { (t, u) ->
                                Log.e(TAG, "已选择: $t  ->  $u")
                            }
                        }

                        Intent(this, BatchScanActivity::class.java).apply {
                            BluetoothRepository.disconnect()
                            BluetoothRepository.mModule = mDeviceInfo.number
                            try {
                                BluetoothRepository.mVersionRange = Range(
                                    binding.versionStart.text.toString().toInt(),
                                    binding.versionEnd.text.toString().toInt()
                                )
                            } catch (e: Exception) {
                                try {
                                    BluetoothRepository.mVersionRange = Range(
                                        binding.versionEnd.text.toString().toInt(),
                                        binding.versionStart.text.toString().toInt()
                                    )
                                } catch (e: Exception) {
                                    BluetoothRepository.mVersionRange = Range(0, Int.MAX_VALUE)
                                }
                            }
                            startActivity(this)
                            finish()
                        }
                    } else {
                        showDialog(getString(R.string.interval_message_4))
                    }
                } else {
                    Toast.makeText(
                        this, getString(R.string.no_selected_batch_modification), Toast.LENGTH_SHORT
                    ).show()
                    val tipsDialog = TipsDialog(this)
                    tipsDialog.setInfo(getString(R.string.select_modified_batch))
                    tipsDialog.show()
                }
            }
        }

        binding.nameEdit.addTextChangedListener {
            mDeviceInfoMap[CommandBean.COMMAND_NAME] = it.toString()
        }
        binding.pinEdit.addTextChangedListener {
            mDeviceInfoMap[CommandBean.COMMAND_PIN] = it.toString()
        }
        binding.intervalEdit.addTextChangedListener {
            mDeviceInfoMap[CommandBean.COMMAND_ADVIN] = it.toString()

        }
        binding.txPowerSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    mDeviceInfoMap[CommandBean.COMMAND_TX_POWER] = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.tlmButton.setOnClickListener {
            mDeviceInfoMap[CommandBean.COMMAND_TLM] = if (binding.tlmButton.isChecked) "1" else "0"
        }
        binding.longRangeCheckBox.setOnClickListener {
            mDeviceInfoMap[CommandBean.COMMAND_ADVEXT] =
                if (binding.longRangeCheckBox.isChecked) "1" else "0"
        }
        binding.keyAdvin.addTextChangedListener {
            val strings = mDeviceInfoMap[CommandBean.COMMAND_KEYCFG] as Array<String>
            strings[0] = it.toString()
            mDeviceInfoMap[CommandBean.COMMAND_KEYCFG] = strings
        }
        binding.keyDuration.addTextChangedListener {
            val strings = mDeviceInfoMap[CommandBean.COMMAND_KEYCFG] as Array<String>
            strings[1] = it.toString()
            mDeviceInfoMap[CommandBean.COMMAND_KEYCFG] = strings
        }
        binding.gsensorAdvinEdit.addTextChangedListener {
            val strings = mDeviceInfoMap[CommandBean.COMMAND_GSCFG] as Array<String>
            strings[0] = it.toString()
            mDeviceInfoMap[CommandBean.COMMAND_GSCFG] = strings
        }
        binding.gsensorDurationEdit.addTextChangedListener {
            val strings = mDeviceInfoMap[CommandBean.COMMAND_GSCFG] as Array<String>
            strings[1] = it.toString()
            mDeviceInfoMap[CommandBean.COMMAND_GSCFG] = strings
        }
        binding.gsensorSensitivityEdit.addTextChangedListener {
            val strings = mDeviceInfoMap[CommandBean.COMMAND_GSCFG] as Array<String>
            strings[2] = it.toString()
            mDeviceInfoMap[CommandBean.COMMAND_GSCFG] = strings
        }

    }

    private fun verify(): Boolean {
        val interval =
            if (binding.intervalEdit.text.isNotEmpty() && binding.intervalCheckBox.isChecked) {
                binding.intervalEdit.text.toString().toInt()
            } else {
                0
            }
        val gSensor =
            if (binding.gsensorAdvinEdit.text.isNotEmpty() && binding.gsensorCheckBox.isChecked) {
                binding.gsensorAdvinEdit.text.toString().toInt()
            } else {
                0
            }
        val keyCfg = if (binding.keyAdvin.text.isNotEmpty() && binding.keyCheckBox.isChecked) {
            binding.keyAdvin.text.toString().toInt()
        } else {
            0
        }
        if (!binding.intervalCheckBox.isChecked && !binding.gsensorCheckBox.isChecked && !binding.keyCheckBox.isChecked) {
            return true
        }
        return if (interval <= 10000 && interval != 0) {
            true
        } else if (gSensor <= 10000 && gSensor != 0) {
            true
        } else {
            keyCfg <= 10000 && keyCfg != 0
        }
    }

    private fun onDeviceInfo(parameterName: String?, parameter: Any?) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (parameterName) {
                CommandBean.COMMAND_BEGIN -> {
                }
                CommandBean.COMMAND_VERSION -> {
                    parameter as String
                    val version = parameter.replace(".", "").replace("V", "")
                    binding.versionStart.setText(version)
                    binding.versionEnd.setText(version)
                }
                CommandBean.COMMAND_ADVIN -> {
                    binding.intervalEdit.setText(parameter as String)
                }
                CommandBean.COMMAND_KEYCFG -> {
                    val s = parameter as Array<String>
                    binding.keyGroup.visibility = View.VISIBLE
                    binding.keyAdvin.setText(s[0])
                    binding.keyDuration.setText(s[1])
                }
                CommandBean.COMMAND_GSCFG -> {
                    binding.gsensorGroup.visibility = View.VISIBLE
                    val s = parameter as Array<*>
                    binding.gsensorAdvinEdit.setText(s[0] as String)
                    binding.gsensorDurationEdit.setText(s[1] as String)
                    if (s.size == 3) {
                        binding.gsensorSensitivityEdit.visibility = View.VISIBLE
                        binding.gsensorSensitivityEdit.setText(s[2] as String)
                    }
                }
                CommandBean.COMMAND_BUZ, CommandBean.COMMAND_LED -> {
                }
                CommandBean.COMMAND_BWMODE -> {
                    when (CommandBean.COMMAND_BWMODE_OPEN) {
                        parameter as String -> {
                        }
                        else -> {
                        }
                    }
                }
                CommandBean.COMMAND_PIN -> {
                    val temp = parameter as String
                    binding.pinEdit.setText(
                        when (temp.length) {
                            1 -> {
                                "00000$temp"
                            }
                            2 -> {
                                "0000$temp"
                            }
                            3 -> {
                                "000$temp"
                            }
                            4 -> {
                                "00$temp"
                            }
                            5 -> {
                                "0$temp"
                            }
                            else -> {
                                temp
                            }
                        }
                    )
                }
                CommandBean.COMMAND_TX_POWER -> {
                    try {
                        (parameter as String).toInt().let {
                            if ("BP671" == mDeviceInfo.name) {
                                val selectIndex = -1

                                binding.txPowerSpinner.setSelection(selectIndex)
                            } else {
                                binding.txPowerSpinner.setSelection(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,"e => ${e.message}")
                    }
                }
                CommandBean.COMMAND_BADVDATA -> {

                }
                CommandBean.COMMAND_EXTEND -> {
                }
                CommandBean.COMMAND_TLM -> {
                    binding.tlmGroup.visibility = View.VISIBLE
                    binding.tlmButton.isChecked = (parameter == "1")
                }
                CommandBean.COMMAND_ADVEXT -> {
                    binding.longRandGroup.visibility = View.VISIBLE
                    binding.longRangeButton.isChecked = (parameter == "1")
                }
                CommandBean.COMMAND_NAME -> {
                    binding.nameEdit.setText(parameter as String)
                }
            }
        }
    }

    private fun showDialog(message: String) {
        val alertDialog2 = AlertDialog.Builder(this).setTitle(R.string.error).setMessage(message)
            .setIcon(R.mipmap.ic_launcher).setNegativeButton(R.string.cancel) { _, _ -> }.create()
        alertDialog2.show()
    }

    companion object {
        private const val TAG = "BatchConfigActivity"
    }

}