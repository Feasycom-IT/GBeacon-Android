package com.glowbird.gbeacon.ui.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.glowbird.gbeacon.logic.BeaconRepository
import com.glowbird.gbeacon.logic.model.FeedbackParams

/**
 * 反馈 ViewMode，用于处理反馈相关逻辑
 */

class FeedbackViewModel : ViewModel() {

    // LiveData 用于存储反馈请求参数，并观察反馈响应
    private val feedbackLiveData = MutableLiveData<FeedbackParams.Params>()

    // Transformations.switchMap 用于将 feedbackLiveData 转换成对应的反馈响应 LiveData
    val mFeedbackResponseBodyLiveData = Transformations.switchMap(feedbackLiveData) {
        BeaconRepository.feedback(it.content, it.feedbackType,it.email)
    }

    /**
     * 发起反馈请求
     * @param content 反馈内容
     * @param feedbackType 反馈类型
     */
    fun feedback(content: String, feedbackType: Int,email: String) {
        // 设置反馈请求参数，并更新 LiveData
        feedbackLiveData.value = FeedbackParams.Params(content, feedbackType, email)
    }

}