package com.glowbird.gbeacon.logic.model

import androidx.annotation.Keep

@Keep
data class AdParams(val platform: String, val appBoundId: String)

@Keep
data class AdResponse(
    val code: Int,
    val `data`: AdData,
    val msg: String
)

@Keep
data class AdData(
    val ID: Int,
    val appkey: String,
    val cnimg: String,
    val cnurl1: String,
    val cnurl2: String,
    val cnurl3: String,
    val createTime: String,
    val enimg: String,
    val enurl1: String,
    val enurl2: String,
    val enurl3: String,
    val title: String
)