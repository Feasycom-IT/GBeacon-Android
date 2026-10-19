package com.glowbird.gbeacon.ui.addbeacon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.feasycn.bean.BeaconBean
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.bean.FeasyBeacon
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityAddBeaconBinding
import com.glowbird.gbeacon.ui.base.BaseActivity

class AddBeaconActivity : BaseActivity<ActivityAddBeaconBinding>() {

    private lateinit var beaconBean: BeaconBean
    private lateinit var mDevice: BluetoothDeviceWrapper
    private var beaconTypeList = arrayListOf(" ", "UID", "URL", "IBeacon", "AltBeacon")

    override fun getViewBinding() = ActivityAddBeaconBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconBean = BeaconBean()
        with(binding) {
            settingParameterEddystoneUrl.setBeaconBean(beaconBean)
            settingParameterEddystoneUid.setBeaconBean(beaconBean)
            settingParameterIbeacon.setBeaconBean(beaconBean)
            settingParameterAltbeacon.setBeaconBean(beaconBean)
        }
        mDevice = intent.getSerializableExtra("dev_") as BluetoothDeviceWrapper
    }

    override fun initView() {
        with(binding){
            header.headerTitle.text = resources.getString(R.string.add_beacon_title)
            header.headerLeft.text = resources.getString(R.string.back)
            header.headerRight.text = resources.getString(R.string.add_beacon_right)
            beaconType.adapter = ArrayAdapter(this@AddBeaconActivity, android.R.layout.simple_spinner_dropdown_item, beaconTypeList)
        }
    }

    override fun initEvent() {
        with(binding){
            header.headerRight.setOnClickListener {
                val intent = Intent().apply {
                    putExtra("beaconBean", beaconBean)
                }
                setResult(REQUEST_BEACON_ADD_OK, intent)
                finish()
            }
            header.headerLeft.setOnClickListener { finish() }
            beaconType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (mDevice.name.contains("BP104EL")) {
                        beaconBean.parentName = "BP104EL"
                    } else if (mDevice.name.contains("BP104EA")) {
                        beaconBean.parentName = "BP104EA"
                    } else if (mDevice.name.contains("BP104E 2nd")) {
                        beaconBean.parentName = "BP104E 2nd"
                    }
                    when (position) {
                        0 -> hideAllParameterViews()
                        1 -> showEddystoneUidParameterView()
                        2 -> showEddystoneUrlParameterView()
                        3 -> showIBeaconParameterView()
                        4 -> showAltBeaconParameterView()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun hideAllParameterViews() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_NULL
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
        }
    }

    private fun showEddystoneUidParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.VISIBLE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterEddystoneUid.setBeaconInfo(beaconBean)
            settingParameterEddystoneUid.setShowType(1)
        }
    }

    private fun showEddystoneUrlParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.VISIBLE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterEddystoneUrl.setBeaconInfo(beaconBean)
            settingParameterEddystoneUrl.setShowType(1)
        }
    }

    private fun showIBeaconParameterView() {
        with(binding){
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_I_BEACON
            settingParameterIbeacon.visibility = View.VISIBLE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterIbeacon.setBeaconInfo(beaconBean)
            settingParameterIbeacon.setShowType(1)
        }
    }

    private fun showAltBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_ALTBEACON
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.VISIBLE
            settingParameterAltbeacon.setBeaconInfo(beaconBean)
            settingParameterAltbeacon.setShowType(1)
        }
    }

    companion object {
        const val REQUEST_BEACON_ADD_OK = 2
    }

}