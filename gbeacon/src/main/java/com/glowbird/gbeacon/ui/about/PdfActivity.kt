package com.glowbird.gbeacon.ui.about

import com.glowbird.gbeacon.R
import com.glowbird.gbeacon.databinding.ActivityPdfBinding
import com.glowbird.gbeacon.ui.base.BaseActivity

/**
 * PDF 文档展示页面
 */

class PdfActivity : BaseActivity<ActivityPdfBinding>() {

    /**
     * 初始化视图
     */
    override fun initView() {
        // 设置标题和返回按钮文本
        binding.header.apply {
            headerTitle.text = resources.getString(R.string.manual)
            headerLeft.text = resources.getString(R.string.back)
            // 返回按钮点击事件
            headerLeft.setOnClickListener { finish() }
        }
        // 加载 PDF 文档
   /*     binding.pdfView.fromAsset("GBeaconAPP.pdf").enableSwipe(true) // 是否允许翻页，默认是允许翻页
            .swipeHorizontal(false) // PDF 文档翻页是否是垂直翻页，默认是左右滑动翻页
            .enableDoubletap(false) // 是否允许双击放大，默认为 true
            .enableAnnotationRendering(false) // 渲染注释（如注释、颜色或表单）
            .password(null) // 设置密码
            .scrollHandle(null) // 滚动句柄
            .enableAntialiasing(true) // 改善低分辨率屏幕上的渲染
            .spacing(0) // 页面间的间距
            .load() // 加载 PDF 文档*/
    }

    /**
     * 获取视图绑定对象
     */
    override fun getViewBinding() = ActivityPdfBinding.inflate(layoutInflater)

}