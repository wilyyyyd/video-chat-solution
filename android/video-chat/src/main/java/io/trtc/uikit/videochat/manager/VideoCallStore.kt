package io.trtc.uikit.videochat.manager

import android.content.Context
import android.content.Intent
import com.tencent.cloud.tuikit.engine.call.TUICallDefine
import com.tencent.cloud.tuikit.engine.common.ContextProvider
import com.tencent.liteav.base.Log
import com.tencent.qcloud.tuicore.TUIConfig
import com.tencent.qcloud.tuicore.interfaces.ITUIService
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.utils.PermissionRequest
import io.trtc.uikit.videochat.page.call.VideoCallPage
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import com.trtc.tuikit.common.foregroundservice.AudioForegroundService
import com.trtc.tuikit.common.foregroundservice.VideoForegroundService
import com.trtc.tuikit.common.permission.PermissionCallback
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.call.CallListener
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallParams
import io.trtc.tuikit.atomicxcore.api.call.CallParticipantStatus
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.login.LoginStore

class VideoCallStore private constructor() : ITUIService {
    private val context: Context = ContextProvider.getApplicationContext()
    private val callStatusObserver = object : CallListener() {
        override fun onCallReceived(callId: String, mediaType: CallMediaType, userData: String) {
            startVideoCallActivity()
        }
    }

    init {
        CallStore.shared.addListener(callStatusObserver)
    }

    internal fun calls(userIdList: List<String>, mediaType: CallMediaType, params: CallParams?, completion: CompletionHandler?) {
        val selfStatus = CallStore.shared.observerState.selfInfo.value.status
        if (selfStatus != CallParticipantStatus.None) {
            completion?.onFailure(TUICallDefine.ERROR_PARAM_INVALID, "You are currently on a call.")
            VideoChatToast.show(context.getString(R.string.videochat_toast_error_already_in_call))
            return
        }
        val list = userIdList.distinct().toMutableList()
        if (list.isEmpty()) {
            Log.e(TAG, "calls failed, userIdList is empty")
            completion?.onFailure(TUICallDefine.ERROR_PARAM_INVALID, "calls failed, userIdList is empty")
            return
        }
        if (list.singleOrNull() == LoginStore.shared.loginState.loginUserInfo.value?.userID) {
            VideoChatToast.show(context.getString(R.string.videochat_toast_error_call_self))
            completion?.onFailure(TUICallDefine.ERROR_PARAM_INVALID, "calls failed, you cannot call yourself")
            return
        }
        PermissionRequest.requestPermissions(context, mediaType, object : PermissionCallback() {
            override fun onGranted() {
                CallStore.shared.calls(list, mediaType, params, object : CompletionHandler {
                    override fun onFailure(code: Int, desc: String) {
                        completion?.onFailure(code, desc)
                    }

                    override fun onSuccess() {
                        completion?.onSuccess()
                        startVideoCallActivity()
                    }

                })
            }
            override fun onDenied() {
                Log.w(TAG, "calls, request Permissions failed")
                completion?.onFailure(TUICallDefine.ERROR_PERMISSION_DENIED, "request Permissions failed")
            }
        })
    }

    internal fun startForegroundService() {
        val inviteeIdsSize = CallStore.shared.observerState.activeCall.value.inviteeIds.size
        val scene: TUICallDefine.Scene = if (inviteeIdsSize == 0) {
            TUICallDefine.Scene.NONE
        } else if (inviteeIdsSize == 1) {
            TUICallDefine.Scene.SINGLE_CALL
        } else {
            TUICallDefine.Scene.GROUP_CALL
        }
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        if (scene == TUICallDefine.Scene.GROUP_CALL || scene == TUICallDefine.Scene.MULTI_CALL
            || mediaType == CallMediaType.Video
        ) {
            VideoForegroundService.start(TUIConfig.getAppContext(), "", "", 0)
        } else if (mediaType == CallMediaType.Audio) {
            AudioForegroundService.start(TUIConfig.getAppContext(), "", "", 0)
        }
    }

    internal fun hangup(completion: CompletionHandler?) {
        Log.i(TAG, "hangup")
        CallStore.shared.hangup(object : CompletionHandler {
            override fun onSuccess() {
                completion?.onSuccess()
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e(TAG, "hangup failed, errorCode: $code, errMsg: $desc")
                completion?.onFailure(code, desc)
            }
        })
    }

    internal fun reject(completion: CompletionHandler?) {
        Log.i(TAG, "reject")
        CallStore.shared.reject(object : CompletionHandler {
            override fun onSuccess() {
                completion?.onSuccess()
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e(TAG, "reject failed, errorCode: $code, errMsg: $desc")
                completion?.onFailure(code, desc)
            }
        })
    }

    internal fun queryOfflineCall() {
        val selfUser = CallStore.shared.observerState.selfInfo.value
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        if (CallParticipantStatus.None == selfUser.status) {
            return
        }

        if (null == mediaType) {
            return
        }
        startVideoCallActivity()
    }

    internal fun selfIsCaller() : Boolean {
        val selfId = CallStore.shared.observerState.selfInfo.value.id
        val callerId = CallStore.shared.observerState.activeCall.value.inviterId
        return selfId == callerId
    }

    internal fun stopForegroundService() {
        VideoForegroundService.stop(TUIConfig.getAppContext())
        AudioForegroundService.stop(TUIConfig.getAppContext())
    }

    private fun startVideoCallActivity() {
        val intent = Intent(context, VideoCallPage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    companion object {
        private const val TAG = "VideoCallStore"
        val shared by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { VideoCallStore() }
    }
}