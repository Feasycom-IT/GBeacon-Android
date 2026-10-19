package com.glowbird.gbeacon.logic.model

/**
 * 表示反馈参数的数据类。
 * @property markdown Markdown 内容和其相关属性。
 * @property msgtype 消息类型，默认为 "markdown"。
 */

data class FeedbackParams(
    val markdown: Markdown,
    val msgtype: String = "markdown"
) {
    /**
     * 包含 Markdown 内容的数据类
     * @property content Markdown 内容
     */
    data class Markdown(
        val content: String
    )

    /**
     * 包含反馈参数的数据类
     * @property content 反馈内容
     * @property feedbackType 反馈类型
     */
    data class Params(
        val content: String,
        val feedbackType: Int,
        val email: String
    )

}

