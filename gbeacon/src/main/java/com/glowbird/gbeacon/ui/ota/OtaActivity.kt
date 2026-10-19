package com.glowbird.gbeacon.ui.ota

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.feasycn.bean.BluetoothDeviceWrapper
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityOtaBinding
import com.glowbird.gbeacon.logic.BluetoothRepository
import com.glowbird.gbeacon.logic.dao.DeviceDatabase
import com.glowbird.gbeacon.logic.interfaces.FscBleCallback
import com.glowbird.gbeacon.logic.network.BeaconNetwork
import com.glowbird.gbeacon.logic.utils.Utils
import com.glowbird.gbeacon.logic.utils.getStr
import com.glowbird.gbeacon.logic.utils.putStr
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.glowbird.gbeacon.ui.dialog.DfuNameDialogFragment
import com.glowbird.gbeacon.ui.dialog.ProgressBarDialogFragment
import com.glowbird.gbeacon.ui.utils.isExternalStorageLegacy
import com.glowbird.gbeacon.ui.utils.isKitkatOrAbove
import com.feasycn.util.FeasyBeaconUtil
import com.feasycn.util.FileUtil
import com.feasycn.util.ToastUtil
import com.glowbird.gbeacon.App.Companion.mBeaconItemDao
import com.glowbird.gbeacon.logic.model.Devices
import com.glowbird.gbeacon.ui.utils.assetsToJsonString
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.*
import java.lang.Runnable
import java.net.UnknownHostException

class OtaActivity : BaseActivity<ActivityOtaBinding>(), FscBleCallback {

    private var mFileNameLiveData = MutableLiveData<String>()
    private var mFileUriLiveData = MutableLiveData<Uri>()
    private val mFileByteArrayLiveData = MutableLiveData<ByteArray>()
    private var mIsDisconnect = false
    private val mProgress = ProgressBarDialogFragment()
    private val mDeviceDao by lazy { DeviceDatabase.getDatabase(this).deviceDao() }
    private val mDevice by lazy { intent.getSerializableExtra("device") as BluetoothDeviceWrapper }
    private val mDevicePin by lazy { intent.getStringExtra("pin") ?: "000000" }
    private var dialog: androidx.appcompat.app.AlertDialog? = null

    private val mHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            START_DOWNLOAD -> {
                mProgress.show(supportFragmentManager, "ota")
            }

            DOWNLOAD_FINISH -> {
                mProgress.dismiss()
            }

            SHOW_DIALOG -> {
                mProgress.dismiss()
                com.glowbird.gbeacon.ui.utils.showDialogs(this, msg.data.getString("message", ""))
            }

            UPDATE_FAILED -> {
            }
        }
        false
    }

    val mDisconnectRunnable = { BluetoothRepository.disconnect() }



    private val mRunnable = Runnable {
        if (null != binding){
            AlertDialog.Builder(this).setTitle(R.string.error)
                .setMessage(getString(R.string.ota_failed)).setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setNegativeButton(R.string.ok) { dialog, _ ->
                    BluetoothRepository.disconnect()
                    dialog.dismiss()
                }.create().show()
        }

    }


    override fun getViewBinding() = ActivityOtaBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        binding.header.headerTitle.text = resources.getString(R.string.update)
        binding.header.headerLeft.text = resources.getString(R.string.back)

        mFileUriLiveData.observe(this) { uri ->
            putStr("fileUri", uri.toString())
            getUriInfo(uri)
        }

        mFileNameLiveData.observe(this) { fileName ->
            binding.otaFileName.text = fileName
        }


        mDevice.let {
            binding.currentVersion.text = it.feasyBeacon.version
            Log.e("vvvv","=========feasyBeacon.module==========${it.feasyBeacon.module}")
            binding.currentModule.text = FeasyBeaconUtil.moduleAdapter(it.feasyBeacon.module)
            mIsDisconnect = true
            BluetoothRepository.disconnect()
        }

        mFileByteArrayLiveData.observe(this) { byteArray ->
            if (byteArray == null || byteArray.isEmpty()) {
                Log.e(TAG, "byteArray is null or empty")
                binding.startOTA.isEnabled = false
                return@observe
            }

            try {
                BluetoothRepository.checkDfuFile(byteArray).let { dfuFileInfo ->
                    binding.exceptVersion.text =
                        "${Integer.valueOf(dfuFileInfo.versonStart)}-${Integer.valueOf(dfuFileInfo.verson_soft_end)}"
                    binding.exceptBLVersion.text =
                        Integer.valueOf(dfuFileInfo.bootloader).toString()
                    lifecycleScope.launch(Dispatchers.IO) {

                        val mDeviceInfo =
                            mBeaconItemDao.queryDeviceByNumber(mDevice.feasyBeacon.module.toInt())
                                ?: run {
                                    // 如果数据库中未找到设备信息，则从 JSON 文件中加载
                                    val json = assetsToJsonString("Device.json")
                                    val gson = Gson()
                                    val devices = gson.fromJson(json, Devices::class.java)
                                    devices?.data?.list?.find { it.number == mDevice.feasyBeacon.module.toInt() }

                                }

                        mDeviceInfo?.let {

                            runOnUiThread {

                                try {
                                    binding.exceptModule.text = mDeviceInfo.name

                                    if (binding.currentModule.text.toString() == binding.exceptModule.text.toString()) {
                                        binding.startOTA.isEnabled = true
                                    } else {
                                        binding.startOTA.isEnabled = false
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("vvvvv", "=============${e.toString()}")
                                    binding.exceptModule.text = "Unknown"
                                    binding.startOTA.isEnabled = false

                                }
                                Log.e(
                                    "vvvv",
                                    "=====模型名称=========${binding.currentModule.text.toString()}      ${binding.exceptModule.text.toString()}     ${mDeviceInfo.name}"
                                )

                            }


                        }


                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "e => ${e.message}")
                binding.startOTA.isEnabled = false
            }

        }

        mFileUriLiveData.observe(this) {
            if (it != null) {
                putStr("FileUri", it.toString())
            }
        }


    }

    override fun onResume() {
        super.onResume()
        getFileUri()
    }

    private fun getFileUri() {
        getStr("fileUri", "").let {
            if (it.isNotEmpty()) {
                mFileUriLiveData.value = Uri.parse(it)
            }
        }
    }

    override fun initEvent() {
        binding.selectFileImage.setOnClickListener { performFileSearch() }
        val downloadClickListener = View.OnClickListener {
            if (isExternalStorageLegacy()) {
                createDfuDialog()
            } else {
                checkPermission()
            }
        }
        binding.downloadFileImg.setOnClickListener(downloadClickListener)

        binding.startOTA.setOnClickListener {
            if (mFileByteArrayLiveData.value != null) {
                binding.selectFileConstraint.visibility = View.VISIBLE
                binding.OTAInfoLL.visibility = View.VISIBLE
                binding.resetLL.visibility = View.GONE
                binding.fileLinear.visibility = View.VISIBLE
                binding.startOTA.visibility = View.GONE
                binding.otaProgressLL.visibility = View.VISIBLE

                mHandler.postDelayed(mRunnable, 20*1000)
                binding.otaProgress.progress = 0
                binding.selectFileImage.isEnabled = false
                binding.selectFileTv.isEnabled = false
                binding.downloadFileImg.isEnabled = false
                binding.downloadFileTv.isEnabled = false

                BluetoothRepository.connect(mDevice, mDevicePin)
            } else {
                binding.selectFileConstraint.visibility = View.VISIBLE
                binding.OTAInfoLL.visibility = View.VISIBLE
                binding.resetLL.visibility = View.VISIBLE
                binding.fileLinear.visibility = View.VISIBLE
                binding.startOTA.visibility = View.VISIBLE
                binding.otaProgressLL.visibility = View.GONE
            }
        }
        binding.header.headerLeft.setOnClickListener {
            BluetoothRepository.disconnect()
            finish()
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (dialog != null) {
                dialog!!.dismiss()
                dialog = null
            }
            dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.tips))
                .setMessage(resources.getString(R.string.file_access_permissions))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(
                        this@OtaActivity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                    )
                }.create()
            dialog!!.show()
        } else {
            createDfuDialog()
        }
    }

    override fun onOtaProgressUpdate(percentage: Int) {
        super.onOtaProgressUpdate(percentage)
        if (percentage != 0) {
            mHandler.removeCallbacks(mRunnable)
        }
        runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    binding.otaProgress.progress = percentage
                    if (percentage == 100) {
                        binding.otaProgress.setmTxtHint1(getString(R.string.ota_success))
                        binding.otaProgress.setmTxtHint2(getString(R.string.ota_success_tips))
                        mHandler.postDelayed(mDisconnectRunnable, 3000)
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    return
                }
            }
        })
    }

    override fun onConnectedSuccess() {
        super.onConnectedSuccess()
        mHandler.removeCallbacks(mDisconnectRunnable)
        mHandler.removeCallbacks(mRunnable)

        BluetoothRepository.startOtaUpdate(mFileByteArrayLiveData.value!!, binding.reset.isChecked)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        mHandler.removeCallbacks(mDisconnectRunnable)
        mHandler.removeCallbacks(mRunnable)
        if (binding.otaProgressLL.visibility == View.VISIBLE) {
            runOnUiThread {
                ToastUtil.show(this@OtaActivity, getString(R.string.disconnect))
                finish()
            }
        } else {
            if (mIsDisconnect) {
                mIsDisconnect = false
            } else {
                runOnUiThread {
                    ToastUtil.show(this@OtaActivity, getString(R.string.disconnect))
                    finish()
                }
            }
        }
    }

    override fun onPacketReceived(strValue: String, hexString: String, rawValue: ByteArray) {
        super.onPacketReceived(strValue, hexString, rawValue)
        if (strValue.contains("OK")) {
            if (strValue.length >= 15) {
                runOnUiThread {
                    val moduleBootloaderInt =
                        Integer.valueOf(FileUtil.stringToInt(strValue.substring(7, 11)))
                    binding.currentBLVersion.text = moduleBootloaderInt.toString()
                }
            } else {
            }
        } else if (strValue.contains("C")) {
        } else if (strValue.contains("S")) {
        } else if ((rawValue[0].toInt() != 0x06) && (rawValue[0].toInt() != 0x15) && (rawValue[0].toInt() != 0x7F)) {
        } else if (hexString == "18") {
            BluetoothRepository.disconnect()
            ToastUtil.show(this@OtaActivity, getString(R.string.ota_failed))
        }
    }

    private fun performFileSearch() {
        val intent: Intent = if (isKitkatOrAbove()) {
            Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/octet-stream"
        startActivityForResult(intent, READ_FILE_REQUEST_CODE)
    }

    private fun getUriInfo(uri: Uri) {
        when (uri.scheme) {
            "content" -> {
                try {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            contentResolver.openInputStream(uri)?.use {
                                mFileByteArrayLiveData.postValue(it.readBytes())
                            }
                            val fileName =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                            mFileNameLiveData.postValue(fileName)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            "file" -> {
                try {
                    uri.toFile().let { file ->
                        mFileNameLiveData.postValue(file.name)
                        mFileByteArrayLiveData.postValue(file.readBytes())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != READ_FILE_REQUEST_CODE || resultCode != RESULT_OK) {
            return
        }
        data?.data?.let { uri ->
            mFileUriLiveData.postValue(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createDfuDialog()
                }
            }
        }
    }

    private fun createDfuDialog() {
        val dfuNameDialogFragment = DfuNameDialogFragment().apply {
            onClickComplete = {
                dialog?.dismiss()
                mHandler.sendEmptyMessage(START_DOWNLOAD)
                if (it.isNotBlank()) {
                    GlobalScope.launch { downloadDfu(it) }
                } else {
                    mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                }
            }
        }
        if (!this.isFinishing) {
            try {
                dfuNameDialogFragment.show(supportFragmentManager, null)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun downloadDfu(parameter: String) {
        coroutineScope {
            deleteExistsFile(parameter)
            try {
                BeaconNetwork.downloadDFU(parameter).apply {
                    if (Utils.isExternalStorageLegacy()) {
                        val contentUri = MediaStore.Downloads.getContentUri("external")
                        val contentValues = ContentValues()
                        contentValues.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/dfu"
                        )
                        contentValues.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            "${parameter}.dfu"
                        )
                        launch(Dispatchers.IO) {
                            // 创建文件
                            contentResolver.insert(contentUri, contentValues)?.let {
                                contentResolver.openOutputStream(it)?.use { out ->
                                    val byteArray = ByteArray(1024)
                                    var len: Int
                                    while (byteStream().read(byteArray).also { len = it } != -1) {
                                        out.write(byteArray, 0, len)
                                    }
                                }
                                mFileUriLiveData.postValue(it)
                            }
                            runOnUiThread {
                                Toast.makeText(
                                    this@OtaActivity,
                                    getString(R.string.down_file_finish),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                        }
                        return@coroutineScope
                    } else {
                        val directoryPath =
                            "${File.separator}sdcard${File.separator}Download${File.separator}dfu${File.separator}"
                        val directory = File(directoryPath)
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val replaceParameter = parameter.replace("/", "_")
                        val path = "${directoryPath}${replaceParameter}.dfu"
                        val file = File(path)
                        launch(Dispatchers.IO) {
                            BufferedInputStream(byteStream()).use { input ->
                                BufferedOutputStream(FileOutputStream(file)).use { out ->
                                    var data: Int
                                    while (input.read().also { data = it } != -1) {
                                        out.write(data)
                                    }
                                }
                            }
                            runOnUiThread {
                                Toast.makeText(
                                    this@OtaActivity,
                                    getString(R.string.down_file_finish),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                            mFileUriLiveData.postValue(file.toUri())
                        }
                    }
                }
            } catch (e: HttpException) {
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString(
                    "message", when (e.code()) {
                        404 -> {
                            getString(R.string.down_file_error)
                        }

                        else -> {
                            getString(R.string.network_error)
                        }
                    }
                )
                mHandler.sendMessage(message)
            } catch (e: IOException) {
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString("message", getString(R.string.io_error))
                mHandler.sendMessage(message)
            } catch (e: UnknownHostException) {
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString("message", getString(R.string.network_error))
                mHandler.sendMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteExistsFile(parameter: String) {
        val replaceParameter = parameter.replace("/", "_")
        if (isExternalStorageLegacy()) {
            val externalContentUri = MediaStore.Downloads.getContentUri("external")
            val selection = MediaStore.Downloads.DISPLAY_NAME + "=?"
            val arg = arrayOf("$replaceParameter.dfu")
            val cursor = contentResolver.query(
                externalContentUri,
                null,
                selection,
                arg,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndexOrThrow = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    val queryId = it.getLong(columnIndexOrThrow)
                    val uri = ContentUris.withAppendedId(externalContentUri, queryId)
                    contentResolver.delete(uri, null, null)
                }
            }
        } else {
            // 不是分区存储
            val path =
                "${File.separator}sdcard${File.separator}Download${File.separator}dfu${File.separator}${replaceParameter}"
            val file = File(path)
            val exists = file.exists()
            if (exists) {
                file.delete()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothRepository.disconnect()
        BluetoothRepository.unRegisterViewCallback(this)
        mHandler.removeCallbacks(mRunnable)
    }

    companion object {
        private const val TAG = "OtaActivity"
        private const val START_DOWNLOAD = 0x0001
        private const val DOWNLOAD_FINISH = 0x0002
        private const val SHOW_DIALOG = 0x0003
        private const val UPDATE_FAILED = 0x0004
        private const val READ_FILE_REQUEST_CODE = 42

        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

}