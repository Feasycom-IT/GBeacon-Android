package com.glowbird.gbeacon

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.glowbird.gbeacon.logic.dao.DeviceDatabase
import com.glowbird.gbeacon.ui.CrashActivity
import com.glowbird.gbeacon.ui.MainActivity
import me.jessyan.autosize.AutoSizeConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        sContext = applicationContext

        AutoSizeConfig.getInstance().setExcludeFontScale(true)


        //AutoSizeConfig.getInstance().setCustomFragment(true);
        //AutoSizeConfig.getInstance().setUseDeviceSize(true);
        AutoSizeConfig.getInstance().setBaseOnWidth(true)


        //    int screenHeight = (int) (AutoSizeConfig.getInstance().getInitScreenWidthDp() + (AutoSizeConfig.getInstance().getInitScreenWidthDp() * 0.1));
        //    Log.e("vvvv", "===========screenHeight============" + screenHeight);
        AutoSizeConfig.getInstance().setDesignWidthInDp(375)

        configureCrashHandling()
    }

    private fun configureCrashHandling() {
       CaocConfig.Builder.create().apply {
            backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
            enabled(true)
            trackActivities(true)
            minTimeBetweenCrashesMs(2000)
            restartActivity(MainActivity::class.java)
            errorActivity(CrashActivity::class.java)
        }.apply()
    }

    companion object {
        const val TAG = "BeaconAPP"
        @SuppressLint("StaticFieldLeak")
        lateinit var sContext: Context
        val mBeaconItemDao by lazy {
            DeviceDatabase.getDatabase(sContext).deviceDao()
        }
    }

}