package com.glowbird.gbeacon.logic.utils

import android.content.Context
import java.util.*

private const val FILE_NAME = "feasybeacon"

/**
 * 获取 SharedPreferences 实例
 *
 * @return SharedPreferences 实例
 */
private fun Context.preference() = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

/**
 * 获取布尔型的值
 *
 * @param key 键
 * @param default 默认值（默认为 true）
 * @return 对应键的布尔值
 */
fun Context.getBoolean(key: String, default: Boolean = true) = preference().getBoolean(key, default)

/**
 * 获取字符串类型的值
 *
 * @param key 键
 * @param default 默认值（默认为空字符串）
 * @return 对应键的字符串
 */
fun Context.getStr(key: String, default: String = "") = preference().getString(key, default) ?: default

/**
 * 获取整数类型的值
 *
 * @param key 键
 * @param default 默认值（默认为 0）
 * @return 对应键的整数值
 */
fun Context.getInt(key: String, default: Int = 0) = preference().getInt(key, default)

/**
 * 获取长整形的值
 *
 * @param key 键
 * @param default 默认值（默认为 0L）
 * @return 对应键的长整型值
 */
fun Context.getLong(key: String, default: Long = 0L) = preference().getLong(key, default)

/**
 * 获取字符串集合类型的值
 *
 * @param key 键
 * @param default 默认值（为 HashSet<String>）
 * @return 对应键的字符串集合值
 */
fun Context.getStrSet(key: String, default: HashSet<String>) = preference().getStringSet(key, default) as HashSet<String>

/**
 * 存储布尔类型的值
 *
 * @param key 键
 * @param value 布尔值
 */
fun Context.putBoolean(key: String, value: Boolean) = preference().edit().putBoolean(key, value).apply()

/**
 * 存储字符串类型的值
 *
 * @param key 键
 * @param value 字符串值
 */
fun Context.putStr(key: String, value: String) = preference().edit().putString(key, value).apply()

/**
 * 存储整数类型的值
 *
 * @param key 键
 * @param value 值
 */
fun Context.putInt(key: String, value: Int) = preference().edit().putInt(key, value).apply()

/**
 * 存储长整型的值。
 *
 * @param key 键
 * @param value 长整型值
 */
fun Context.putLong(key: String, value: Long) = preference().edit().putLong(key, value).apply()

/**
 * 存储字符串集合类型的值。
 *
 * @param key 键
 * @param value 字符串集合值
 */
fun Context.putStrSet(key: String, value: HashSet<String>) = preference().edit().putStringSet(key, value).apply()

/**
 * 移除指定键的数据。
 *
 * @param key 键
 */
fun Context.removeData(key: String?) = key?.let { preference().edit().remove(key).apply() }

