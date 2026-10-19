package com.glowbird.gbeacon.ui.utils

import android.content.Context
import android.util.Log
import com.glowbird.gbeacon.logic.model.Devices
import com.google.gson.Gson
import java.io.File
import java.io.IOException

class JsonFileManager(private val context: Context) {

    private val TAG = "JsonFileManager"

    // 写入 JSON 数据到内部存储中的 Device.json 文件
    fun writeJsonToInternalStorage(fileName: String, jsonContent: String) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonContent.toByteArray())
            }
            Log.i(TAG, "JSON data has been written to $fileName in internal storage")
        } catch (e: IOException) {
            Log.e(TAG, "Error writing to file: $fileName", e)
        }
    }

    fun parseLocalDeviceData(fileName: String): Devices? {
        return Gson().fromJson(readJsonFromInternalStorage(fileName), Devices::class.java)
    }

    // 从内部存储中的 Device.json 文件读取 JSON 数据
    private fun readJsonFromInternalStorage(fileName: String): String {
        return try {
            if (isFileExists(fileName)) {
                context.openFileInput(fileName).bufferedReader().use { reader ->
                    reader.readText()
                }
            } else {
                ""
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading file: $fileName", e)
            ""
        }
    }

    // 检查文件是否存在
    private fun isFileExists(fileName: String): Boolean {
        Log.e(TAG, "isFileExists filesDir => ${context.filesDir}")
        val file = File(context.filesDir, fileName)
        return file.exists()
    }
}
