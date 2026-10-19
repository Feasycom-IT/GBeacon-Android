package com.glowbird.gbeacon.logic.utils

import android.os.Build
import android.os.Environment

/**
 * 工具类，提供设备和系统相关的实用函数。
 */
object Utils {

    /**
     * 检查设备是否运行在 Android 4.4 (KitKat) 或更高版本。
     *
     * @return 如果设备运行在 Android 4.4 或更高版本，返回 true；否则返回 false。
     */
    fun isKitkatOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    /**
     * 检查设备是否使用外部存储的新模式。
     * 在 Android 10 及更高版本中，该模式称为“外部存储遗留模式”。
     *
     * @return 如果设备使用外部存储的新模式，返回 true；否则返回 false。
     */
    fun isExternalStorageLegacy(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()
    }
}
