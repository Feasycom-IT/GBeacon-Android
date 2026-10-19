package com.glowbird.gbeacon.ui.setting

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.glowbird.gbeacon.R

import com.glowbird.gbeacon.databinding.ActivityScancodeBinding
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.mylhyl.zxing.scanner.OnScannerCompletionListener


class ScanQrActivity : BaseActivity<ActivityScancodeBinding>() {


    override fun getViewBinding() = ActivityScancodeBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        binding.header.apply {
            headerLeft.text = resources.getString(R.string.back)
            headerTitle.text = getString(R.string.scaning)
            // 返回按钮点击事件
            headerLeft.setOnClickListener { finish() }
        }

    }


    override fun initEvent() {
        super.initEvent()
        binding.scannerView.setDrawText(getString(R.string.scaned),true)
        binding.scannerView.setOnScannerCompletionListener(OnScannerCompletionListener { result1, result2, bitmap ->

            if (TextUtils.isEmpty(result1.text)) {
                ToastUtils.showLong("Please rescan！")
                binding.scannerView.restartPreviewAfterDelay(1000)
            } else {
                /*   val intent = Intent()
                   intent.putExtra("str_data", result1.text)
                   setResult(Activity.RESULT_OK, intent)
                   finish()*/

                ScanCallback.getInstance().scall.result(result1.text)
                /*      val intent = Intent()
                      intent.putExtra(
                          "code_type", 888
                      )
                      intent.putExtra("code", result1.text)
                      setResult(Activity.RESULT_OK, intent)*/
                finish()


            }

        })
    }





    override fun onResume() {
        super.onResume()
        binding.scannerView.onResume()
    }


    override fun onPause() {
        super.onPause()
        binding.scannerView.onPause()
    }


}