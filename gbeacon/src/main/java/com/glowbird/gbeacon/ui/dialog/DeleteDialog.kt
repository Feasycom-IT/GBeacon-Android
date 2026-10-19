package com.glowbird.gbeacon.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.feasycn.bean.BeaconBean
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.logic.BluetoothRepository

/**
 * 删除对话框，继承自基础对话框类 BaseDialog
 */

class DeleteDialog(context: Context?) : BaseDialog(context), View.OnClickListener {

    // 取消按钮
    lateinit var deleteNo: Button
    // 确认删除按钮
    lateinit var deleteYes: Button
    // 待删除的 BeaconBean 对象
    var beaconBean: BeaconBean? = null

    /**
     * 初始化对话框布局和UI
     */
    private fun initUI() {
        val view  = layoutInflater.inflate(R.layout.dialog_delete, null, false)
        val layoutParams  = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 添加对话框内容视图
        addContentView(view, layoutParams)
        setCanceledOnTouchOutside(false)
        // 初始化取消按钮并设置点击事件监听器
        deleteNo = findViewById(R.id.delete_no)
        deleteNo.setOnClickListener(this)
        // 初始化确认删除按钮并设置点击事件监听器
        deleteYes = findViewById(R.id.delete_yes)
        deleteYes.setOnClickListener(this)
    }

    /**
     * 点击按钮的处理逻辑
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.delete_no -> super.dismiss() // 点击取消按钮，隐藏对话框
            R.id.delete_yes -> {
                // 点击确认删除按钮，调用删除操作并隐藏对话框
                beaconBean?.let {
                    BluetoothRepository.deleteBeaconInfo(it)
                }
                super.dismiss()
            }
        }
    }

    init {
        initUI()
    }

}