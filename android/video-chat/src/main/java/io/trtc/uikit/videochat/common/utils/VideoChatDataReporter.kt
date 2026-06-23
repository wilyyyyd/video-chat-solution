package io.trtc.uikit.videochat.common.utils

import com.tencent.imsdk.common.IMLog
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import org.json.JSONObject

class VideoChatDataReporter {
    companion object {
        const val MEET_PAGE = 1601
        const val CONVERSATION_PAGE = 1602
        const val CALL_PAGE = 1603
        const val BEAUTY_PANEL = 1604

        internal fun reportMetrics(type : Int) {
            val param = JSONObject().apply {
                put("UIComponentType", type.toLong())
            }.toString()
            V2TIMManager.getInstance()
                .callExperimentalAPI("reportTUIFeatureUsage", param, object : V2TIMValueCallback<Any> {
                    override fun onSuccess(t: Any?) {
                        // do nothing
                    }

                    override fun onError(code: Int, desc: String?) {
                        IMLog.e("AtomicXCore-DataReporter", "reportFeatureUsage failed: $code $desc")
                    }
                })
        }
    }
}