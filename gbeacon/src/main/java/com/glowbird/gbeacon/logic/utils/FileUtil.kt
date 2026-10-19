package com.glowbird.gbeacon.logic.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 文件操作的工具对象。
 */
object FileUtil {

    /**
     * 将位图保存为图像文件并发送广播以扫描文件。
     *
     * @param context 调用方法的上下文。
     * @param bitmap 要保存的位图。
     * @param bitName 保存位图的文件名。
     * @param isSuccess 回调函数，用于指示保存是否成功。
     */
    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        bitName: String,
        isSuccess: (Boolean) -> Unit
    ) {
        val storagePath = Environment.getExternalStorageDirectory().path + "/DCIM/"
        val fileName = storagePath + bitName
        val file = File(fileName)

        try {
            FileOutputStream(file).use { out ->
                // 压缩位图并保存到文件
                val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                if (success) {
                    Log.i("FileUtil", "位图成功保存: $fileName")
                } else {
                    Log.e("FileUtil", "压缩位图失败: $fileName")
                }
                isSuccess.invoke(success)
            }

            // 发送广播以扫描文件
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$fileName")
                )
            )
            Log.i("FileUtil", "广播已发送以扫描文件: $fileName")
        } catch (e: IOException) {
            isSuccess.invoke(false)
            Log.e("FileUtil", "保存位图时出错: ${e.localizedMessage}")
        }
    }
}
