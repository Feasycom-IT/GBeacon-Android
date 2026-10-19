package com.glowbird.gbeacon.logic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.feasycn.bean.BeaconBean
import com.glowbird.gbeacon.ui.view.LabelEditView
import com.glowbird.gbeacon.logic.BluetoothRepository
import java.lang.Exception

/**
 * 视图实用工具类，提供用于生成和操作视图的辅助函数。
 */

object ViewUtil {

    private const val TAG = "ViewUtil"

    /**
     * 将像素值转换为 DIP 值，确保相同的尺寸在不同设备上保持一致。
     * @param context 上下文
     * @param pxValue 像素值
     * @return 转换后的 DIP 值
     */
    fun pxToDip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将 DIP 值转换为像素值，确保相同的尺寸在不同设备上保持一致。
     * @param context 上下文
     * @param dipValue DIP 值
     * @return 转换后的像素值
     */
    fun dipToPx(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * 将像素值转换为 SP 值，确保文本大小在不同设备上保持一致。
     * @param context 上下文
     * @param pxValue 像素值
     * @return 转换后的 SP 值
     */
    fun pxToSp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 将 SP 值转换为像素值，确保文本大小在不同设备上保持一致。
     * @param context 上下文
     * @param spValue SP 值
     * @return 转换后的像素值
     */
    fun spToPx(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 将 DP 值转换为像素值，确保相同的尺寸在不同设备上保持一致。
     * @param dp DP 值
     * @param res 资源对象
     * @return 转换后的像素值
     */
    fun dpToPx(dp: Float, res: Resources): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics
        ).toInt()
    }

    /**
     * 设置 EditText 和 TextView 的颜色为红色。
     * @param editText 编辑框
     * @param textView 文本框
     */
    fun setLabelEditRed(editText: EditText, textView: TextView) {
        editText.setTextColor(Color.RED)
        textView.setTextColor(Color.RED)
    }

    /**
     * 设置 EditText 和 TextView 的颜色为默认颜色。
     * @param editText 编辑框
     * @param textView 文本框
     */
    fun setLabelEditBlock(editText: EditText, textView: TextView) {
        editText.setTextColor(-0x848485)
        textView.setTextColor(-0xe2e2e3)
    }

    /**
     * 监听 EditText 的点击事件，使光标始终位于文本的末尾。
     */
    class ClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val e = v as EditText
            e.requestFocus()
            e.setSelection(e.text.length)
        }
    }

    /**
     * 监听 EditText 的触摸事件，使光标始终位于文本的末尾。
     */
    class TouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val e = v as EditText
            e.requestFocus()
            e.setSelection(e.text.length)
            return false
        }
    }

    /**
     * PowerTextWatcher 监听器，用于监控 EditText 中的输入并进行相应的处理。
     * @param textView 文本框
     * @param editText 编辑框
     * @param beaconBean 信标对象
     */
    class RssiTextWatcher(
        var textView: TextView, var editText: EditText, var beaconBean: BeaconBean
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            val value = s.toString()
            try {
                if (value.length >= 5 || value.isEmpty() || value == "-" || value.contains("--")) {
                    setLabelEditRed(editText, textView)
                    beaconBean.rssi = ""
                    return
                }
                val intValue = value.toIntOrNull()
                if (intValue in -128..127) {
                    setLabelEditBlock(editText, textView)
                    beaconBean.rssi = value
                } else {
                    setLabelEditRed(editText, textView)
                    beaconBean.rssi = ""
                }
            } catch (e: Exception) {
                setLabelEditRed(editText, textView)
                beaconBean.rssi = ""
            }
        }
    }

    /**
     * 监视 EditText 中的设备名称输入，并在输入发生变化时进行相应处理。
     * @param labelEditView 标签编辑视图，用于显示错误或正常状态
     */
    class NameTextWatcher(
        private val labelEditView: LabelEditView
    ) : TextWatcher {
        private var isFirstChange = true

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // 未使用，不做任何操作
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // 未使用，不做任何操作
        }

        override fun afterTextChanged(s: Editable) {
            val deviceName = s.toString()
            if (deviceName.isEmpty()) {
                // 如果设备名称为空，则设置为错误状态
                labelEditView.setRed()
            } else {
                if (isFirstChange) {
                    isFirstChange = false
                } else {
                    // 设置设备名称并将视图设置为正常状态
                    BluetoothRepository.setDeviceName(deviceName)
                }
                labelEditView.setBlock()
            }
        }
    }


    /**
     * 监视 PIN 输入的 TextWatcher 实现，确保输入符合 PIN 长度和格式限制。
     * @param labelEditView 用于显示错误和输入格式化的 LabelEditView
     */
    class PinTextWatcher(
        private val labelEditView: LabelEditView
    ) : TextWatcher {
        private var isFirstChange = true

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // 设置输入类型为数字
            labelEditView.setInputType(EditorInfo.TYPE_CLASS_NUMBER)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // 在文本改变时不执行任何操作
        }

        override fun afterTextChanged(s: Editable) {
            val devicePin = s.toString()
            if (devicePin.length == 6) {
                // 如果 PIN 长度为 6，则设置为有效
                labelEditView.setBlock()
                if (!isFirstChange) {
                    BluetoothRepository.setDevicePin(devicePin)
                } else {
                    isFirstChange = false
                }
            } else {
                // 如果 PIN 长度不为 6，则设置为无效
                labelEditView.setRed()
                if (!isFirstChange) {
                    BluetoothRepository.setDevicePin("000000")
                } else {
                    isFirstChange = false
                }
            }
        }
    }


}