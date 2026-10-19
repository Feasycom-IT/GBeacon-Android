package com.glowbird.gbeacon.ui.about

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityQrcodeBinding
import com.glowbird.gbeacon.logic.utils.FileUtil
import com.glowbird.gbeacon.ui.base.BaseActivity
import com.permissionx.guolindev.PermissionX

/**
 * 二维码展示页面
 */

class QRCodeActivity : BaseActivity<ActivityQrcodeBinding>() {

    // 保存结果对话框
    private lateinit var resultDialog: AlertDialog

    /**
     * 初始化视图
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        // 设置返回按钮文本和标题
        binding.header.apply {
            headerLeft.text = resources.getString(R.string.back)
            headerTitle.text = getString(R.string.wx)
            // 返回按钮点击事件
            headerLeft.setOnClickListener { finish() }
        }
        // 设置图片资源
        binding.img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.wx))
    }

    private fun query(): Boolean {
        val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Images.Media.DISPLAY_NAME + "=?"
        val arg = arrayOf("wx.jpg")
        val cursor = contentResolver.query(
            externalContentUri, null, selection, arg, null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val columnIndexOrThrow = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            Log.e(TAG, "query: " + cursor.getLong(columnIndexOrThrow))
            val long = cursor.getLong(columnIndexOrThrow)
            cursor.close()
            long != 0L
        } else {
            false
        }
    }

    /**
     * 查询图片是否存在
     */
    @SuppressLint("Recycle")
    private fun isUriExist(uri: Uri): Boolean {
        val cursor = contentResolver.query(
            uri, null, null, null, null
        )
        return cursor != null && cursor.moveToFirst()
    }

    companion object {
        private const val TAG = "QRCodeActivity"
    }

    /**
     * 获取视图绑定对象。
     */
    override fun getViewBinding() = ActivityQrcodeBinding.inflate(layoutInflater)

    /**
     * 初始化事件。
     */
    override fun initEvent() {
        // 保存按钮点击事件
        binding.save.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q 及以上版本
                saveImageAndroidQAndAbove()
            } else {
                // Android Q 以下版本
                // 请求存储权限
                PermissionX.init(this).permissions(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).request { allGranted, _, _ ->
                    if (allGranted) {
                        saveImageBelowAndroidQ()
                    } else {
                        // 权限被拒绝
                        val dialog = AlertDialog.Builder(this@QRCodeActivity)
                            .setMessage(getString(R.string.permission_denied))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                        dialog.setCanceledOnTouchOutside(false)
                    }
                }
            }
        }
    }

    private fun saveImageAndroidQAndAbove() {
        val imgName = "wx.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, imgName)
            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                // 将图片保存到媒体库
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.wx)
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    showResultDialog(getString(R.string.qr_success))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showResultDialog(getString(R.string.qr_failed))
            }
        } ?: run {
            showResultDialog(getString(R.string.qr_failed))
        }
    }

    private fun saveImageBelowAndroidQ() {
        FileUtil.saveBitmap(
            this,
            BitmapFactory.decodeResource(resources, R.drawable.wx),
            "wechat.jpg"
        ) {
            showResultDialog(getString(if (it) R.string.qr_success else R.string.qr_failed))
        }
    }

    private fun showResultDialog(message: String) {
        val dialog = AlertDialog.Builder(this@QRCodeActivity)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        dialog.setCanceledOnTouchOutside(false)
    }

}