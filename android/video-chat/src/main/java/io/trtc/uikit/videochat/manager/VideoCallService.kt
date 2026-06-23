package io.trtc.uikit.videochat.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.tencent.cloud.tuikit.engine.common.ContextProvider
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMManager.V2TIM_STATUS_LOGINED
import com.tencent.qcloud.tuicore.ServiceInitializer
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.interfaces.ITUIExtension
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import com.tencent.qcloud.tuicore.interfaces.ITUIService
import com.tencent.qcloud.tuicore.interfaces.TUIExtensionEventListener
import com.tencent.qcloud.tuicore.interfaces.TUIExtensionInfo
import io.trtc.uikit.videochat.R
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallParams
import io.trtc.uikit.videochat.page.call.VideoCallPage
import java.lang.ref.WeakReference

class VideoCallService : ServiceInitializer(), ITUIService {
    private var appContext: Context? = null
    private var callPageRef = WeakReference<VideoCallPage>(null)
    private val tuiExtension = object : ITUIExtension {
        override fun onGetExtension(extensionID: String?, param: Map<String?, Any?>?): List<TUIExtensionInfo?>? {
            if (TextUtils.equals(extensionID, TUIConstants.TUIChat.Extension.InputMore.CLASSIC_EXTENSION_ID)) {
                return getClassicChatInputMoreExtension(param)
            }
            return null
        }
    }
    private val tuiNotification = object : ITUINotification {
        override fun onNotifyEvent(key: String?, subKey: String?, param: MutableMap<String, Any>?) {
            when (subKey) {
                TUIConstants.TUILogin.EVENT_SUB_KEY_START_INIT -> {
                    UserInfoStore.shared
                    VideoCallStore.shared
                }
            }
        }
    }

    override fun init(context: Context) {
        appContext = ContextProvider.getApplicationContext()
        TUICore.registerExtension(TUIConstants.TUIChat.Extension.InputMore.CLASSIC_EXTENSION_ID, tuiExtension)
        TUICore.registerEvent(TUIConstants.TUILogin.EVENT_IMSDK_INIT_STATE_CHANGED, TUIConstants.TUILogin.EVENT_SUB_KEY_START_INIT, tuiNotification)
        registerActivityLifecycleCallbacks(context)
    }

    private fun registerActivityLifecycleCallbacks(context: Context) {
        if (context is Application) {
            context.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                private var isCallPageStopped = false
                override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {
                    if (activity is VideoCallPage) {
                        isCallPageStopped = false
                    }
                }

                override fun onActivityResumed(activity: Activity) {
                    if (activity is VideoCallPage) {
                        callPageRef = WeakReference(activity)
                        return
                    }
                    ensureCallPageInForeground()
                }
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {
                    if (activity is VideoCallPage) {
                        isCallPageStopped = true
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    if (activity is VideoCallPage && activity == callPageRef.get()) {
                        callPageRef.clear()
                    }
                }

                private fun ensureCallPageInForeground() {
                    val callPage = callPageRef.get() ?: return
                    if (!isCallPageStopped) return
                    if (callPage.isInPictureInPictureMode) return
                    if (V2TIMManager.getInstance().loginStatus != V2TIM_STATUS_LOGINED) return
                    VideoCallStore.shared.queryOfflineCall()
                }
            })
        }
    }

    private fun getClassicChatInputMoreExtension(param: Map<String?, Any?>?): List<TUIExtensionInfo>? {
        val videoCallExtension = TUIExtensionInfo()
        videoCallExtension.weight = 500
        val userID: String? = getOrDefault<String>(param, TUIConstants.TUIChat.Extension.InputMore.USER_ID, null)
        val videoListener = ResultTUIExtensionEventListener()
        videoListener.mediaType = CallMediaType.Video
        videoListener.userID = userID
        val extensionInfoList: MutableList<TUIExtensionInfo> = ArrayList()
        videoCallExtension.text = appContext?.getString(R.string.videochat_text_video_call)
        videoCallExtension.icon = R.drawable.videochat_ic_video_call
        videoCallExtension.extensionListener = videoListener
        val filterVideo: Boolean =
            getOrDefault(param, TUIConstants.TUIChat.Extension.InputMore.FILTER_VIDEO_CALL, false) == true
        if (!filterVideo) {
            extensionInfoList.add(videoCallExtension)
        }
        return extensionInfoList
    }

    override fun onCall(method: String?, param: Map<String?, Any?>?): Any? {
        Log.i(TAG, "onCall, method: $method ,param: $param")
        if (TextUtils.isEmpty(method)) {
            return null
        }

        if (null != param && TextUtils.equals(TUIConstants.TUICalling.METHOD_NAME_CALL, method)) {
            val userIDs = getOrDefault<Array<String>>(param, TUIConstants.TUICalling.PARAM_NAME_USERIDS, null)
            var userIdList: List<String?>? = userIDs?.toList() ?: ArrayList()
            Log.i(TAG, "onCall, userIdList: $userIdList")
            userIdList = userIdList?.filterNotNull()
            if (userIdList != null) {
                startCall(userIdList, CallMediaType.Video)
            }
        }
        return null
    }

    inner class ResultTUIExtensionEventListener : TUIExtensionEventListener() {
        var mediaType: CallMediaType = CallMediaType.Video
        var userID: String? = null
        override fun onClicked(param: Map<String, Any>?) {
            if (!userID.isNullOrEmpty()) {
                val userList = mutableListOf<String>()
                userList.add(userID!!)
                startCall(userList, mediaType)
            }
        }
    }

    private fun startCall(userIdList: List<String>, mediaType: CallMediaType) {
        val params = CallParams()
        params.timeout = 30
        VideoCallStore.shared.calls(userIdList, mediaType, params, null)
    }

    private fun <T> getOrDefault(map: Map<*, *>?, key: Any, defaultValue: T?): T? {
        if (map == null || map.isEmpty()) {
            return defaultValue
        }
        val value = map[key]
        try {
            if (value != null) {
                return value as T
            }
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return defaultValue
    }

    companion object {
        private const val TAG = "CallKitService"
    }
}