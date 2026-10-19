package com.glowbird.gbeacon.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils

/**
 * BaseActivity 是所有 Activity 的基类，用于简化 ViewBinding 的初始化和公共操作。
 * @param T 表示 ViewBinding 类型的泛型参数。
 */

abstract class BaseActivity2<T : ViewBinding> : AppCompatActivity() {

    // ViewBinding 对象，延迟初始化
    private lateinit var _binding: T

    // 公共的获取 ViewBinding 对象的方法
    protected val binding: T
        get() = _binding

    // Activity 创建时的回调方法
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BarUtils.transparentStatusBar(this@BaseActivity2)
        BarUtils.setStatusBarLightMode(this@BaseActivity2, true)

        // 初始化 ViewBinding
        _binding = getViewBinding()
        // 设置布局内容为 ViewBinding 的根布局
        setContentView(binding.root)
        // 初始化界面
        initView()
        // 初始化事件
        initEvent()
    }

    /**
     * 获取 ViewBinding 对象的抽象方法，子类必须实现。
     * @return 返回 ViewBinding 对象。
     */
    abstract fun getViewBinding(): T

    /**
     * 初始化界面的方法，子类可选择性重写。
     */
    open fun initView() {}

    /**
     * 初始化事件的方法，子类可选择性重写。
     */
    open fun initEvent() {}

}