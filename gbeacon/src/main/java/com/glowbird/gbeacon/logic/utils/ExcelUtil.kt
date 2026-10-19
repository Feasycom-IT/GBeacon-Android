package com.glowbird.gbeacon.logic.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.logic.model.Order
import com.feasycn.util.ToastUtil
import jxl.Workbook
import jxl.format.Colour
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import jxl.write.WritableSheet
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat

/**
 * 将订单列表写入Excel文件
 *
 * @param presetMap 预设参数映射
 * @param orders 订单列表
 * @param fileName 文件名
 * @return 保存的文件路径
 */
fun Context.writeToExcel(
    context: Context,
    presetMap: LinkedHashMap<String, Any>,
    orders: List<Order>,
    fileName: String
): String {
    // Excel列标题
    val titles = arrayOf("TIME", "NAME", "ADDRESS", "CONNECT")

    // 创建文件名
    val formattedFileName = createFormattedFileName(fileName)

    // 根据Android版本创建存储文件的URI
    val uri = createUriForFile(formattedFileName)

    uri?.let {
        try {
            contentResolver.openOutputStream(it)?.use { os ->
                writeDataToExcel(context,os, presetMap, orders, titles)
                ToastUtil.show(this, getString(R.string.write_succeeded))
                return formattedFileName
            }
        } catch (e: IOException) {
            Log.e("TAG", "写入Excel失败", e)
            ToastUtil.show(this, getString(R.string.write_failed))
        }
    }
    return ""
}

/**
 * 根据文件名生成格式化文件名
 *
 * @param fileName 原始文件名
 * @return 格式化后的文件名
 */
private fun createFormattedFileName(fileName: String): String {
    return if (fileName == "TIME") {
        "${SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())}.xls"
    } else {
        "$fileName.xls"
    }
}

/**
 * 根据文件名创建存储文件的URI
 *
 * @param fileName 文件名
 * @return 文件的URI
 */
private fun Context.createUriForFile(fileName: String): Uri? {
    val storagePath = "${Environment.DIRECTORY_DOWNLOADS}/BatchModificationRecord"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.RELATIVE_PATH, storagePath)
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/vnd.ms-excel")
        }
        contentResolver.insert(MediaStore.Downloads.getContentUri("external"), contentValues)
    } else {
        // 旧版本Android直接创建文件路径
        val directory = File(Environment.getExternalStorageDirectory().path + "/$storagePath")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, fileName)
        Uri.fromFile(file)
    }
}

/**
 * 将订单列表写入Excel文件
 *
 * @param os 输出流
 * @param presetMap 预设参数映射
 * @param orders 订单列表
 * @param titles 列标题
 */
private fun writeDataToExcel(
    context: Context,
    os: OutputStream,
    presetMap: LinkedHashMap<String, Any>,
    orders: List<Order>,
    titles: Array<String>
) {
    val workbook = Workbook.createWorkbook(os)
    val sheet = workbook.createSheet(context.getString(R.string.batch_modify_records), 0)

    // 写入预设参数
    writePresetMapToSheet(sheet, presetMap)

    // 写入列标题
    titles.forEachIndexed { index, title ->
        sheet.addCell(Label(index, 3, title, createHeaderCellFormat()))
        sheet.setColumnView(index, 20)
    }

    // 写入订单数据
    writeOrdersToSheet(sheet, orders, titles)

    workbook.write()
    workbook.close()
}

/**
 * 将预设参数写入工作表
 *
 * @param sheet 工作表
 * @param presetMap 预设参数映射
 */
private fun writePresetMapToSheet(sheet: WritableSheet, presetMap: LinkedHashMap<String, Any>) {
    // 定义一个变量来跟踪列的偏移量
    var columnOffset = 0

    // 遍历 presetMap 中的键值对
    for ((index, entry) in presetMap.entries.withIndex()) {
        val key = entry.key
        val value = entry.value

        // 添加标题单元格并设置格式
        sheet.addCell(Label(index + columnOffset, 0, key, createHeaderCellFormat()))
        sheet.setColumnView(index + columnOffset, 20) // 设置列宽度

        // 处理不同类型的值
        when (value) {
            is Array<*> -> {
                // 如果值是数组，则遍历数组元素
                value.forEachIndexed { i, param ->
                    // 添加数组元素单元格
                    sheet.addCell(
                        Label(
                            index + columnOffset + i,
                            1,
                            param.toString(),
                            createCenteredCellFormat()
                        )
                    )

                    // 如果数组长度大于1，则合并单元格
                    if (value.size > 1 && i == 0) {
                        sheet.mergeCells(index + columnOffset, 0, index + columnOffset + value.size - 1, 0)
                    }
                    // 更新列偏移量
                    columnOffset++
                }
            }
            else -> {
                // 如果值是单个元素，则添加单元格
                sheet.addCell(
                    Label(
                        index + columnOffset, 1, value.toString(), createCenteredCellFormat()
                    )
                )
            }
        }
        // 在每次循环结束后，增加列偏移量
        columnOffset++
    }
}

/**
 * 将订单数据写入工作表
 *
 * @param sheet 工作表
 * @param orders 订单列表
 * @param titles 列标题
 */
private fun writeOrdersToSheet(sheet: WritableSheet, orders: List<Order>, titles: Array<String>) {
    orders.forEachIndexed { index, order ->
        sheet.addCell(Label(0, index + 4, order.time, createCenteredCellFormat()))
        sheet.addCell(Label(1, index + 4, order.name, createCenteredCellFormat()))
        sheet.addCell(Label(2, index + 4, order.address, createCenteredCellFormat()))
        sheet.addCell(Label(3, index + 4, order.connect.toString(), createCenteredCellFormat()))

        order.map?.forEach { (key, value) ->
            val titleIndex = titles.indexOf(key)
            if (titleIndex != -1) {
                sheet.addCell(
                    Label(
                        titleIndex, index + 4, value.toString(), createCenteredCellFormat()
                    )
                )
            }
        }
    }
}

/**
 * 创建表头单元格格式
 *
 * @return WritableCellFormat
 */
private fun createHeaderCellFormat(): WritableCellFormat {
    val font = WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD).apply {
        colour = Colour.BLUE
    }
    return WritableCellFormat(font).apply {
        alignment = jxl.format.Alignment.CENTRE
        verticalAlignment = VerticalAlignment.CENTRE
        setBackground(Colour.YELLOW)
    }
}

/**
 * 创建居中的单元格格式
 *
 * @return WritableCellFormat
 */
private fun createCenteredCellFormat(): WritableCellFormat {
    val font = WritableFont(WritableFont.TIMES, 10).apply {
        colour = Colour.BLACK
    }
    return WritableCellFormat(font).apply {
        alignment = jxl.format.Alignment.CENTRE
        verticalAlignment = VerticalAlignment.CENTRE
    }
}
