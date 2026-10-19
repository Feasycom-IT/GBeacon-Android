package com.glowbird.gbeacon.ui.beacon

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.BatteryManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityCustomBroadcastBinding
import com.glowbird.gbeacon.logic.utils.*
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.setting.*
import com.feasycn.util.ToastUtil
import java.util.regex.Pattern

class CustomBroadcastActivity : BaseActivity<ActivityCustomBroadcastBinding>() {

    private var iBeaconAdvertiser: IBeaconAdvertiser? = null
    private var aoaBeaconAdvertiser: AoABeaconAdvertiser? = null
    private var altBeaconAdvertiser: AltBeaconAdvertiser? = null
    private var eddystoneUIDAdvertiser: EddystoneUIDAdvertiser? = null
    private var eddystoneURLAdvertiser: EddystoneURLAdvertiser? = null
    private var eddystoneTLMAdvertiser: EddystoneTLMAdvertiser? = null
    private var batteryManager: BatteryManager? = null

    override fun getViewBinding() = ActivityCustomBroadcastBinding.inflate(layoutInflater)

    override fun initView() {
        // 设置标题栏
        binding.header.headerLeft.text = getString(R.string.back)
        binding.header.headerTitle.text = getString(R.string.beacon_broadcast)
        binding.header.spinnerBeaconType.visibility = View.VISIBLE

        val beaconTypes = resources.getStringArray(R.array.beacon_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, beaconTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.header.spinnerBeaconType.adapter = adapter

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager?

        binding.header.spinnerBeaconType.setSelection(0)
        switchLayout("iBeacon")

        binding.ibeaconBroadcastUuid.setText(getStr("ibeacon_uuid", ""))
        binding.ibeaconBroadcastMajor.setText(getStr("ibeacon_major", ""))
        binding.ibeaconBroadcastMinor.setText(getStr("ibeacon_minor", ""))
        binding.ibeaconBroadcastRssi.setText(getStr("ibeacon_rssi", ""))
        binding.ibeaconBroadcastSwitch.isChecked = getBoolean("ibeaconBroadcastSwitchOpen", false)

//        aoaBeacon_broadcast_mac.setText(getStr("aoabeacon_mac", ""))
//        aoaBeacon_alarm_switch.isChecked = getInt("aoabeacon_alarm", 0) == 1
//        aoaBeacon_moving_switch.isChecked = getInt("aoabeacon_static", 0) != 0
//
//        val aoabeaconChannel = getInt("aoabeacon_channel", 1)
//        when (aoabeaconChannel) {
//            1 -> Channel37.isChecked = true
//            2 -> Channel38.isChecked = true
//            3 -> Channel39.isChecked = true
//        }
//
//        val aoabeaconMode = getInt("aoabeacon_mode", 2)
//        when (aoabeaconMode) {
//            1 -> MODE_LOW_POWER.isChecked = true
//            2 -> MODE_BALANCED.isChecked = true
//            3 -> MODE_LOW_LATENCY.isChecked = true
//        }
//
//        val aoabeaconTxPower = getInt("aoabeacon_txpower", 3)
//        when (aoabeaconTxPower) {
//            1 -> TX_POWER_ULTRA_LOW.isChecked = true
//            2 -> TX_POWER_LOW.isChecked = true
//            3 -> TX_POWER_MEDIUM.isChecked = true
//            4 -> TX_POWER_HIGH.isChecked = true
//        }
//
//        aoaBeacon_broadcast_switch.isChecked = getBoolean("aoabeaconBroadcastSwitchOpen", false)

        binding.altBeaconBroadcastId1.setText(getStr("altbeacon_id1", ""))
        binding.altBeaconBroadcastId2.setText(getStr("altbeacon_id2", ""))
        binding.altBeaconBroadcastId3.setText(getStr("altbeacon_id3", ""))
        binding.altBeaconBroadcastRssi.setText(getStr("altbeacon_rssi", ""))
        binding.altBeaconBroadcastManufacturerId.setText(getStr("altbeacon_manufacturerId", ""))
        binding.altBeaconBroadcastManufacturerReserved.setText(
            getStr(
                "altbeacon_manufacturerReserved",
                ""
            )
        )
        binding.altBeaconBroadcastSwitch.isChecked = getBoolean("altbeaconBroadcastSwitchOpen", false)

        binding.eddyUidBroadcastNamespace.setText(getStr("eddystone_uid_namespace", ""))
        binding.eddyUidBroadcastInstance.setText(getStr("eddystone_uid_instance", ""))
        binding.eddyUidBroadcastReserved.setText(getStr("eddystone_uid_reserved", ""))
        binding.eddyUidBroadcastRssi.setText(getStr("eddystone_uid_rssi", ""))
        binding.eddyUidBroadcastSwitch.isChecked = getBoolean("eddyUidBroadcastSwitchOpen", false)

        binding.eddyUrlBroadcastUrl.setText(getStr("eddystone_url_url", ""))
        binding.eddyUrlBroadcastRssi.setText(getStr("eddystone_url_rssi", ""))
        binding.eddyUrlBroadcastSwitch.isChecked = getBoolean("eddyUrlBroadcastSwitchOpen", false)

        binding.eddyTlmBroadcastBattery.setText(getStr("eddystone_tlm_battery", ""))
        binding.eddyTlmBroadcastTemperature.setText(getStr("eddystone_tlm_temperature", ""))
        binding.eddyTlmBroadcastCount.setText(getStr("eddystone_tlm_count", ""))
        binding.eddyTlmBroadcastTime.setText(getStr("eddystone_tlm_time", ""))
        binding.eddyTlmBroadcastSwitch.isChecked = getBoolean("eddyTlmBroadcastSwitchOpen", false)

    }

    override fun initEvent() {
        binding.header.headerLeft.setOnClickListener {
            finish()
        }

        binding.header.spinnerBeaconType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedType = parent.getItemAtPosition(position).toString()
                    switchLayout(selectedType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.ibeaconBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            val uuidText = binding.ibeaconBroadcastUuid.text?.toString()
            val majorText = binding.ibeaconBroadcastMajor.text?.toString()
            val minorText = binding.ibeaconBroadcastMinor.text?.toString()
            val rssiText = binding.ibeaconBroadcastRssi.text?.toString()
            if (isChecked) {
                if (uuidText != null && validateUUID(uuidText)) {
                    putStr("ibeacon_uuid", uuidText)
                    val major = majorText?.toIntOrNull()
                    val minor = minorText?.toIntOrNull()
                    val rssi = rssiText?.toIntOrNull()
                    when {
                        majorText.isNullOrBlank() || major == null || major !in 0..65535 -> {
                            showIBeaconInvalidInput(R.string.invalid_major)
                        }
                        minorText.isNullOrBlank() || minor == null || minor !in 0..65535 -> {
                            showIBeaconInvalidInput(R.string.invalid_minor)
                        }
                        rssiText.isNullOrBlank() || rssi == null || rssi !in -100..0 -> {
                            showIBeaconInvalidInput(R.string.invalid_rssi)
                        }
                        else -> {
                            putStr("ibeacon_major", majorText)
                            putStr("ibeacon_minor", minorText)
                            putStr("ibeacon_rssi", rssiText)
                            putBoolean("ibeaconBroadcastSwitchOpen", true)
                            iBeaconAdvertiser = IBeaconAdvertiser()
                            iBeaconAdvertiser?.start(this, uuidText, majorText, minorText, rssiText)
                        }
                    }
                } else {
                    showIBeaconInvalidInput(R.string.invalid_uuid)
                }
            } else {
                disableIBeaconBroadcast()
            }
        }

        binding.aoaBeaconBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            var mac = binding.aoaBeaconBroadcastMac.text?.toString()
            if (isChecked) {
                if (mac.isNullOrEmpty()) {
                    mac = "0000"
                }
//                showAoABeaconInvalidInput(R.string.altbeacon_id2)
                val id: Short = Integer.parseInt(mac, 16).toShort()
                val alarm: Byte = if (binding.aoaBeaconAlarmSwitch.isChecked) 1 else 0
                val isStatic: Byte = if (binding.aoaBeaconMovingSwitch.isChecked) 0 else 1
                val channel: Byte = getChannel()
                val txPower: Byte = getTxPower()
                val mode: Byte = getMode()
                val battery: Byte =
                    (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)!! / 10).toByte()

//                    putStr("aoabeacon_mac", mac)
//                    putInt("aoabeacon_alarm", alarm.toInt())
//                    putInt("aoabeacon_static", isStatic.toInt())
//                    putInt("aoabeacon_channel", channel.toInt())
//                    putInt("aoabeacon_txpower", txPower.toInt())
//                    putInt("aoabeacon_mode", mode.toInt())
//                    putBoolean("aoabeaconBroadcastSwitchOpen", true)
                aoaBeaconAdvertiser = AoABeaconAdvertiser()
                aoaBeaconAdvertiser?.startAdvertising(
                    this,
                    id,
                    alarm,
                    battery,
                    mode,
                    txPower,
                    channel,
                    isStatic
                )
            } else {
                disableAoABeaconBroadcast()
            }
        }

        binding.altBeaconBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            val id1 = binding.altBeaconBroadcastId1.text?.toString()
            val id2 = binding.altBeaconBroadcastId2.text?.toString()
            val id3 = binding.altBeaconBroadcastId3.text?.toString()
            val rssi = binding.altBeaconBroadcastRssi.text?.toString()?.toIntOrNull()
            val manufacturerId = binding.altBeaconBroadcastManufacturerId.text?.toString()
            val manufacturerReserved = binding.altBeaconBroadcastManufacturerReserved.text?.toString()

            if (isChecked) {
                val errorMessage = when {
                    id1.isNullOrEmpty() || id1.length != 32 -> getString(R.string.altbeacon_id1)
                    id2.isNullOrEmpty() || id2.length != 4 -> getString(R.string.altbeacon_id2)
                    id3.isNullOrEmpty() || id3.length != 4 -> getString(R.string.altbeacon_id2)
                    rssi == null || rssi !in -128..127 -> getString(R.string.altbeacon_rssi)
                    manufacturerId.isNullOrEmpty() || manufacturerId.length != 5 -> getString(R.string.altbeacon_manufacturer_id)
                    manufacturerReserved.isNullOrEmpty() || manufacturerReserved.length != 2 -> getString(
                        R.string.altbeacon_manufacturer_reserved
                    )
                    else -> null
                }

                if (errorMessage == null) {
                    id1?.let { putStr("altbeacon_id1", it) }
                    id2?.let { putStr("altbeacon_id2", it) }
                    id3?.let { putStr("altbeacon_id3", it) }
                    rssi?.let { putStr("altbeacon_rssi", it.toString()) }
                    manufacturerId?.let { putStr("altbeacon_manufacturerId", it) }
                    manufacturerReserved?.let { putStr("altbeacon_manufacturerReserved", it) }
                    putBoolean("altbeaconBroadcastSwitchOpen", true)

                    altBeaconAdvertiser = AltBeaconAdvertiser()
                    altBeaconAdvertiser?.startAdvertising(
                        this,
                        id1,
                        id2,
                        id3,
                        rssi.toString(),
                        manufacturerId,
                        manufacturerReserved
                    )
                } else {
                    showAltBeaconInvalidInput(errorMessage)
                }
            } else {
                disableAltBeaconBroadcast()
            }
        }

        binding.eddyUidBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            val namespace = binding.eddyUidBroadcastNamespace.text?.toString()
            val instance = binding.eddyUidBroadcastInstance.text?.toString()
            val reserved = binding.eddyUidBroadcastReserved.text?.toString()
            val rssi = binding.eddyUidBroadcastRssi.text?.toString()?.toIntOrNull()

            if (isChecked) {
                val errorMessage = when {
                    namespace.isNullOrEmpty() || namespace.length != 20 -> getString(R.string.eddyUid_namespace)
                    instance.isNullOrEmpty() || instance.length != 12 -> getString(R.string.eddyUid_instance)
                    reserved.isNullOrEmpty() || reserved.length != 4 -> getString(R.string.altbeacon_id2)
                    rssi == null || rssi !in -128..127 -> getString(R.string.altbeacon_rssi)
                    else -> null
                }

                if (errorMessage == null) {
                    namespace?.let { putStr("eddystone_uid_namespace", it) }
                    instance?.let { putStr("eddystone_uid_instance", it) }
                    reserved?.let { putStr("eddystone_uid_reserved", it) }
                    rssi?.let { putStr("eddystone_uid_rssi", it.toString()) }
                    putBoolean("eddyUidBroadcastSwitchOpen", true)

                    eddystoneUIDAdvertiser = EddystoneUIDAdvertiser()
                    eddystoneUIDAdvertiser?.startAdvertising(
                        this,
                        namespace,
                        instance,
                        reserved,
                        rssi.toString()
                    )
                } else {
                    showEddystoneUIDInvalidInput(errorMessage)
                }
            } else {
                disableEddystoneUIDBroadcast()
            }
        }

        binding.eddyUrlBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            val url = binding.eddyUrlBroadcastUrl.text?.toString()
            val rssi = binding.eddyUrlBroadcastRssi.text?.toString()

            if (isChecked) {
                if (url != null && validateURL(url)) {
                    putStr("eddystone_url_url", url)
                    if (rssi.isNullOrBlank() || rssi == null || rssi?.toIntOrNull() !in -128..127) {
                        showEddystoneURLInvalidInput(R.string.invalid_rssi)
                    } else {
                        putStr("eddystone_url_rssi", rssi)
                        putBoolean("eddyUrlBroadcastSwitchOpen", true)
                        eddystoneURLAdvertiser = EddystoneURLAdvertiser()
                        eddystoneURLAdvertiser?.startAdvertising(this, url, rssi)
                    }
                }
            } else {
                disableEddystoneURLBroadcast()
            }
        }

        binding.eddyTlmBroadcastSwitch.setOnCheckedChangeListener { _, isChecked ->
            val battery = binding.eddyTlmBroadcastBattery.text?.toString()
            val temperature = binding.eddyTlmBroadcastTemperature.text?.toString()
            val count = binding.eddyTlmBroadcastCount.text?.toString()
            val time = binding.eddyTlmBroadcastTime.text?.toString()

            if (isChecked) {
                val errorMessage = when {
                    battery.isNullOrEmpty() || battery.length != 4 -> getString(R.string.altbeacon_id2)
                    temperature.isNullOrEmpty() || temperature.length != 4 -> getString(R.string.altbeacon_id2)
                    count.isNullOrEmpty() || count.length != 8 -> getString(R.string.eddyTlm_count)
                    time.isNullOrEmpty() || time.length != 8 -> getString(R.string.eddyTlm_count)
                    else -> null
                }

                if (errorMessage == null) {
                    battery?.let { putStr("eddystone_tlm_battery", it) }
                    temperature?.let { putStr("eddystone_tlm_temperature", it) }
                    count?.let { putStr("eddystone_tlm_count", it) }
                    time?.let { putStr("eddystone_tlm_time", it) }
                    putBoolean("eddyTlmBroadcastSwitchOpen", true)

                    eddystoneTLMAdvertiser = EddystoneTLMAdvertiser()
                    eddystoneTLMAdvertiser?.startAdvertising(
                        this,
                        battery,
                        temperature,
                        count,
                        time
                    )
                } else {
                    showEddystoneTLMInvalidInput(errorMessage)
                }
            } else {
                disableEddystoneTLMBroadcast()
            }
        }

    }

    @SuppressLint("NonConstantResourceId")
    private fun getChannel(): Byte {
        return when (binding.radioGroupChannel.checkedRadioButtonId) {
            R.id.Channel38 -> 1
            R.id.Channel39 -> 2
            else -> 0
        }
    }

    @SuppressLint("NonConstantResourceId")
    private fun getTxPower(): Byte {
        return when (binding.radioGroupTxPower.checkedRadioButtonId) {
            R.id.TX_POWER_ULTRA_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW.toByte()
            R.id.TX_POWER_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_LOW.toByte()
            R.id.TX_POWER_HIGH -> AdvertiseSettings.ADVERTISE_TX_POWER_HIGH.toByte()
            else -> AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM.toByte()
        }
    }

    @SuppressLint("NonConstantResourceId")
    private fun getMode(): Byte {
        return when (binding.radioGroupMode.checkedRadioButtonId) {
            R.id.MODE_LOW_POWER -> AdvertiseSettings.ADVERTISE_MODE_LOW_POWER.toByte()
            R.id.MODE_LOW_LATENCY -> AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY.toByte()
            else -> AdvertiseSettings.ADVERTISE_MODE_BALANCED.toByte()
        }
    }

    private fun switchLayout(beaconType: String) {
        binding.iBeaconLayout.visibility = View.GONE
        binding.aoaBeaconLayout.visibility = View.GONE
        binding.altBeaconLayout.visibility = View.GONE
        binding.eddyUidLayout.visibility = View.GONE
        binding.eddyUrlLayout.visibility = View.GONE
        binding.eddyTlmLayout.visibility = View.GONE

        when (beaconType) {
            "iBeacon" -> binding.iBeaconLayout.visibility = View.VISIBLE
            "aoaBeacon" -> binding.aoaBeaconLayout.visibility = View.VISIBLE
            "altBeacon" -> binding.altBeaconLayout.visibility = View.VISIBLE
            "eddy-uid" -> binding.eddyUidLayout.visibility = View.VISIBLE
            "eddy-url" -> binding.eddyUrlLayout.visibility = View.VISIBLE
            "eddy-tlm" -> binding.eddyTlmLayout.visibility = View.VISIBLE
        }
    }

    private fun showIBeaconInvalidInput(errorResId: Int) {
        putBoolean("ibeaconBroadcastSwitchOpen", false)
        binding.ibeaconBroadcastSwitch.isChecked = false
        ToastUtil.show(this, getString(errorResId))
    }

    private fun showAoABeaconInvalidInput(errorResId: Int) {
        putBoolean("aoabeaconBroadcastSwitchOpen", false)
        binding.aoaBeaconBroadcastSwitch.isChecked = false
        ToastUtil.show(this, getString(errorResId))
    }

    private fun showAltBeaconInvalidInput(errorResId: String) {
        putBoolean("altbeaconBroadcastSwitchOpen", false)
        binding.altBeaconBroadcastSwitch.isChecked = false
        ToastUtil.show(this, errorResId)
    }

    private fun showEddystoneUIDInvalidInput(errorResId: String) {
        putBoolean("eddyUidBroadcastSwitchOpen", false)
        binding.eddyUidBroadcastSwitch.isChecked = false
        ToastUtil.show(this, errorResId)
    }

    private fun showEddystoneURLInvalidInput(errorResId: Int) {
        putBoolean("eddyUrlBroadcastSwitchOpen", false)
        binding.eddyUrlBroadcastSwitch.isChecked = false
        ToastUtil.show(this, getString(errorResId))
    }

    private fun showEddystoneTLMInvalidInput(errorResId: String) {
        putBoolean("eddyTlmBroadcastSwitchOpen", false)
        binding.eddyTlmBroadcastSwitch.isChecked = false
        ToastUtil.show(this, errorResId)
    }

    private fun disableIBeaconBroadcast() {
        putBoolean("ibeaconBroadcastSwitchOpen", false)
        binding.ibeaconBroadcastSwitch.isChecked = false
        iBeaconAdvertiser?.stopAdvertising()
    }

    private fun disableAoABeaconBroadcast() {
        putBoolean("aoabeaconBroadcastSwitchOpen", false)
        binding.aoaBeaconBroadcastSwitch.isChecked = false
        aoaBeaconAdvertiser?.stopAdvertising()
    }

    private fun disableAltBeaconBroadcast() {
        putBoolean("altbeaconBroadcastSwitchOpen", false)
        binding.altBeaconBroadcastSwitch.isChecked = false
        altBeaconAdvertiser?.stopAdvertising()
    }

    private fun disableEddystoneUIDBroadcast() {
        putBoolean("eddyUidBroadcastSwitchOpen", false)
        binding.eddyUidBroadcastSwitch.isChecked = false
        eddystoneUIDAdvertiser?.stopAdvertising()
    }

    private fun disableEddystoneURLBroadcast() {
        putBoolean("eddyUrlBroadcastSwitchOpen", false)
        binding.eddyUrlBroadcastSwitch.isChecked = false
        eddystoneURLAdvertiser?.stopAdvertising()
    }

    private fun disableEddystoneTLMBroadcast() {
        putBoolean("eddyTlmBroadcastSwitchOpen", false)
        binding.eddyTlmBroadcastSwitch.isChecked = false
        eddystoneTLMAdvertiser?.stopAdvertising()
    }

    private fun validateUUID(uuid: String): Boolean {
        val uuidRegex =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        val pattern = Pattern.compile(uuidRegex)
        val matcher = pattern.matcher(uuid)
        return matcher.matches()
    }

    private fun validateURL(url: String): Boolean {
        val urlRegex =
            """^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([\/\w.-]*)*\/?$"""
        val pattern = Pattern.compile(urlRegex)
        val matcher = pattern.matcher(url)
        return matcher.matches()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableIBeaconBroadcast()
        disableAoABeaconBroadcast()
        disableAltBeaconBroadcast()
        disableEddystoneUIDBroadcast()
        disableEddystoneURLBroadcast()
        disableEddystoneTLMBroadcast()
    }

}