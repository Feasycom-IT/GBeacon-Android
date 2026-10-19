package com.glowbird.gbeacon.ui.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.glowbird.gbeacon.R
import java.io.*
import java.util.*
import kotlin.experimental.and
import kotlin.math.roundToLong

private const val PREFS_READ_STORAGE_PERMISSION_REQUESTED = "read_storage_permission_requested"
private const val PREFS_WRITE_STORAGE_PERMISSION_REQUESTED = "write_storage_permission_requested"

private const val Hex = "0123456789ABCDEF"

/**
 * 判断当前 Android 版本是否为 Kitkat 或更高版本
 */
fun isKitkatOrAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}

/**
 * 判断外部存储是否处于 Legacy 模式（仅适用于 Android Q 及更高版本）
 */
fun isExternalStorageLegacy(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()
}

/**
 * 检查写外部存储权限是否已授予
 */
fun isWriteExternalStoragePermissionsGranted(context: Context?): Boolean {
    return ContextCompat.checkSelfPermission(
        context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * 检查写外部存储权限是否永久拒绝
 */
fun isWriteExternalStoragePermissionDeniedForever(activity: Activity?): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
    return (!isWriteExternalStoragePermissionsGranted(activity) &&
            preferences.getBoolean(
                PREFS_WRITE_STORAGE_PERMISSION_REQUESTED, false
            ) &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
}

/**
 * 将字符串转换为小端的 Int，字符串必须是 4 个数字，不足 4 个数字后面补 0，超过 4 个数字只取前 4 个。
 */
fun stringToInt(string: String): Int {
    val str1 = string.substring(0, 2)
    val str2 = string.substring(2, 4)
    val int1: Int = formattingHexToInt(str1)
    val int2: Int = formattingHexToInt(str2)
    val byte1 = int1.toByte()
    val byte2 = int2.toByte()
    return byteToInt_2(byte1, byte2)
}

/**
 * 格式化十六进制中的字母并转换为对应的数字
 */
fun formattingHexToInt(a: String): Int {
    val a1 = a.substring(0, 1)
    val a2 = a.substring(1, 2)
    return formattingOneHexToInt(a1) * 16 + formattingOneHexToInt(a2)
}

/**
 * 将两个字节转换为 Int
 */
fun byteToInt_2(a: Byte, b: Byte): Int {
    val int_b: Int = (b and 0xff.toByte()).toInt()
    val int_a: Int = (a and 0xff.toByte()).toInt()
    return int_a + (int_b shl 8)
}

/**
 * 将十六进制中的字母转换为对应的数字
 */
fun formattingOneHexToInt(a: String?): Int {
    return if (a == null || a.length != 1) {
        0
    } else Hex.indexOf(a.uppercase(Locale.getDefault()))
}

/**
 * 检查系统版本是否为 Marshmallow 或更高版本
 */
fun isMarshmallowOrAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

/**
 * 检查位置是否已启用
 */
fun isLocationEnabled(context: Context): Boolean {
    if (isMarshmallowOrAbove()) {
        var locationMode = Settings.Secure.LOCATION_MODE_OFF
        try {
            locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            // do nothing
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }
    return true
}

/**
 * 检查蓝牙是否已启用
 */
fun isBleEnabled(): Boolean {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    return adapter != null && adapter.isEnabled
}

/**
 * 复制文本到剪贴板
 */
fun Context.copyText(copiedText: String) {
    val clipboardManager: ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, copiedText))
}

/**
 * 从 Uri 获取文件路径
 */
fun getFilePathFromContentUri(context: Context, uri: Uri?): String? {
    if (null == uri) return null
    var data: String? = null
    val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
    val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
    if (null != cursor) {
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            data = if (index > -1) {
                cursor.getString(index)
            } else {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val fileName = cursor.getString(nameIndex)
                getPathFromInputStreamUri(context, uri, fileName)
            }
        }
        cursor.close()
    }
    return data
}

/**
 * 从 Uri 获取文件路径（通过输入流拷贝文件至应用私有目录）
 */
private fun getPathFromInputStreamUri(context: Context, uri: Uri, fileName: String): String? {
    var inputStream: InputStream? = null
    var filePath: String? = null
    if (uri.authority != null) {
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val file = createTemporalFileFrom(context, inputStream, fileName)
            filePath = file!!.path
        } catch (e: Exception) {
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
            }
        }
    }
    return filePath
}

/**
 * 创建临时文件并从输入流拷贝数据
 */
@Throws(IOException::class)
private fun createTemporalFileFrom(
    context: Context, inputStream: InputStream?, fileName: String
): File? {
    var targetFile: File? = null
    if (inputStream != null) {
        var read: Int
        val buffer = ByteArray(8 * 1024)
        // 自定义拷贝文件路径
        targetFile = File(context.externalCacheDir, fileName)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        val outputStream: OutputStream = FileOutputStream(targetFile)
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return targetFile
}

/**
 * 从 assets 中读取 JSON 文件内容并转换为字符串
 */
fun Context.assetsToJsonString(fileName: String): String {
    val stringBuilder = StringBuilder()
    try {
        assets.open(fileName).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                stringBuilder.append(line)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return stringBuilder.toString()
}

/**
 * 检查指定的 Activity 类是否位于任务栈的顶部
 */
fun isActivityTop(cls: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val topActivityName = manager.getRunningTasks(1).firstOrNull()?.topActivity?.className
    return topActivityName == cls.name
}

/**
 * 将十六进制字符串表示的时间转换为秒数
 * @param hexString 十六进制字符串表示的时间
 * @return 转换后的秒数
 */
fun hexToSeconds(hexString: String): Double {
    // 将十六进制字符串转换为十进制数值
    val decimalValue = hexString.replace(" ", "").toLong(16)
    // 每个单位是0.1秒，因此除以10.0得到秒数
    return decimalValue / 10.0
}

/**
 * 将秒数转换为时间格式（天、小时、分钟、秒）
 * @param seconds 秒数
 * @return 时间格式的字符串，例如 "2 days + 04:36:12.500"
 */
fun secondsToTime(seconds: Double): String {
    // 计算天数
    val days = (seconds / (24 * 3600)).toInt()
    // 计算剩余秒数
    val remainingSeconds = seconds % (24 * 3600)
    // 计算小时数
    val hours = (remainingSeconds / 3600).toInt()
    // 计算剩余分钟数
    val remainingMinutes = (remainingSeconds % 3600) / 60
    // 计算最终剩余秒数
    val remainingSecondsFinal = remainingSeconds % 60

    // 返回时间格式的字符串
    return "${String.format("%.0f", days.toDouble())} days + ${
        String.format(
            "%02d", hours
        )
    }:${String.format("%.0f", remainingMinutes)}:${
        String.format(
            "%.3f", remainingSecondsFinal
        )
    }"
}

/**
 * 将十六进制字符串表示的温度转换为摄氏度温度值。
 *
 * @param hexString 十六进制字符串，包含温度值的整数部分和小数部分的十六进制表示。
 * @return 实际的摄氏度温度值。
 */
fun hexToTemperature(hexString: String): Double {
    // 解析整数部分
    val integerPart = hexString.substring(0, 2).toInt(16).toDouble() // 将十六进制字符串的前两个字符解析为整数部分，并转换为十进制数

    // 解析小数部分
    val fractionalPart = hexString.substring(2).toInt(16).toDouble() / 256.0 // 将十六进制字符串的后两个字符解析为小数部分，并转换为十进制数，再除以256得到小数部分的值

    // 合并整数和小数部分
    var temperature = integerPart + fractionalPart // 将整数部分和小数部分相加得到温度值

    // 保留小数点后六位
    temperature = (temperature * 1e6).roundToLong() / 1e6 // 将温度值乘以1e6，四舍五入取整，再除以1e6，保留小数点后六位

    return temperature // 返回温度值
}

fun showDialogs(context: Context, message: String) {
    val alertDialog =
        AlertDialog.Builder(context).setTitle(R.string.error).setInverseBackgroundForced(false)
            .setMessage(message).setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.ok, null).create()
    alertDialog.show()
}