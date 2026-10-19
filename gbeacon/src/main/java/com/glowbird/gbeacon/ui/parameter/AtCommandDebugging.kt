package com.glowbird.gbeacon.ui.parameter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.AtCommandDebuggingBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.ui.base.BaseActivity
import java.lang.ref.WeakReference

class AtCommandDebugging : BaseActivity<AtCommandDebuggingBinding>(), FscBleCallback {

    private lateinit var mHandler: Handler

    private var stringBuilder = StringBuilder()

    private class MyHandler(activity : AtCommandDebugging) : Handler(Looper.getMainLooper()) {
        private val mWeakReference : WeakReference<AtCommandDebugging> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            mWeakReference.get()?.run {
                when (msg.what) {
                    RECEIVE_DATA -> {
                        val strValue = msg.data.getString("strValue")
                        stringBuilder.append(strValue)
                        binding.receiveDataEt.setText(stringBuilder.toString())
                    }
                }
            }
        }
    }

    override fun getViewBinding() = AtCommandDebuggingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        super.initView()
        with(binding) {
            header.backGroup.visibility = View.VISIBLE
            header.titleText.text = getString(R.string.status_connected)
        }
        mHandler = MyHandler(this)
    }

    override fun initEvent() {
        super.initEvent()
        with(binding) {
            header.backTv.setOnClickListener { finish() }

            clearDataAcb.setOnClickListener {
                binding.receiveDataEt.setText("")
                stringBuilder.clear()
            }

            sendDataAcb.setOnClickListener {
                val msg = sendDataEt.text.toString()
                if (msg.isNotBlank()) {
                    BluetoothRepository.setFirmwareKey(msg)
                }
            }
        }
    }

    override fun onPacketReceived(strValue: String, hexString: String, rawValue: ByteArray) {
        super.onPacketReceived(strValue, hexString, rawValue)
        Log.e("TAG","strValue => $strValue  hexString => $hexString")
        val message = Message()
        message.what = RECEIVE_DATA
        val bundle = Bundle()
        bundle.putString("strValue", strValue)
        message.data = bundle
        mHandler.sendMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothRepository.disconnect()
        mHandler.removeCallbacksAndMessages(null)
        BluetoothRepository.unRegisterViewCallback(this)

    }

    companion object {
        private const val RECEIVE_DATA = 0X0001
    }

}