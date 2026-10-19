package com.glowbird.gbeacon.ui

import com.glowbird.gbeacon.databinding.ActivityMainBinding
import com.glowbird.gbeacon.ui.base.BaseActivity

/**
 * 主界面
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)
}
