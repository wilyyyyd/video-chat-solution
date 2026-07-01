package io.trtc.uikit.videochat.page.call

import android.Manifest
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.tencent.qcloud.tuicore.util.TUIBuild
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.utils.PermissionRequest
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.videochat.manager.VideoCallStore
import io.trtc.uikit.videochat.page.call.hint.InviteeInfoView
import com.trtc.tuikit.common.FullScreenActivity
import io.trtc.uikit.videochat.page.beauty.tebeauty.TEBeautyManager
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.tuikit.common.imageloader.ImageOptions
import com.trtc.tuikit.common.permission.PermissionCallback
import com.trtc.tuikit.common.permission.PermissionRequester
import com.trtc.tuikit.common.ui.floatwindow.FloatWindowManager
import io.trtc.tuikit.atomicxcore.api.call.CallEndReason
import io.trtc.tuikit.atomicxcore.api.call.CallListener
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallParticipantStatus
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.device.AudioRoute
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.view.CallLayoutTemplate
import io.trtc.uikit.videochat.common.utils.VideoChatDataReporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class VideoCallPage : FullScreenActivity() {
    private var videoCallView: CallView? = null
    private var callEndHintView: TextView? = null
    private var calleeInfoView: InviteeInfoView? = null
    private var imageFloatButton: ImageView? = null
    private var finishActivityJob: Job? = null
    private var hideFunctionView: Boolean = false
    private val callStatusObserver = object : CallListener() {
        override fun onCallEnded(callId: String, mediaType: CallMediaType, reason: CallEndReason, userId: String) {
            runOnUiThread {
                handleCallEnded(reason, userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        currentInstance = WeakReference(this)
        setScreenLockParams(window)
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        setContentView(R.layout.videochat_activity_video_call)
        applyWindowInsets()
        openMediaDevice()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        CallStore.shared.addListener(callStatusObserver)
        VideoCallStore.shared.startForegroundService()
        VideoChatDataReporter.reportMetrics(VideoChatDataReporter.CALL_PAGE)
    }

    override fun onResume() {
        super.onResume()
        PermissionRequest.requestPermissions(application, CallMediaType.Video, object : PermissionCallback() {
            override fun onGranted() {
                val callStatus = CallStore.shared.observerState.selfInfo.value.status
                if (CallParticipantStatus.None == callStatus) {
                    finishAndRemoveTask()
                    return
                }
                initView()
            }

            override fun onDenied() {
                if (!VideoCallStore.shared.selfIsCaller()) {
                    CallStore.shared.reject(null)
                }
                finishAndRemoveTask()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        currentInstance = null
        finishActivityJob?.cancel()
        CallStore.shared.removeListener(callStatusObserver)
        VideoCallStore.shared.stopForegroundService()
        TEBeautyManager.clearBeautyView()
        Log.i(TAG, "onDestroy")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val hasAudioPermission = PermissionRequester.newInstance(Manifest.permission.RECORD_AUDIO).has()
        if (!hasAudioPermission) {
            return
        }
        val hasVideoPermission = PermissionRequester.newInstance(Manifest.permission.CAMERA).has()
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        if (mediaType == CallMediaType.Video && !hasVideoPermission) {
            return
        }
        enterPictureInPictureModeWithBuild()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        calleeInfoView?.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        imageFloatButton?.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        if (isInPictureInPictureMode) {
            videoCallView?.setLayoutTemplate(CallLayoutTemplate.Pip)
        } else {
            hangupOnPipWindowClose()
        }
    }

    override fun onBackPressed() {
    }

    private fun hangupOnPipWindowClose() {
        if (lifecycle.currentState != Lifecycle.State.CREATED) {
            return
        }
        val callerId = CallStore.shared.observerState.activeCall.value.inviterId
        val selfId = CallStore.shared.observerState.selfInfo.value.id
        val selfStatus = CallStore.shared.observerState.selfInfo.value.status
        Log.i(TAG, "user close pip window , callerId = $callerId , selfId = $selfId , selfStatus=$selfStatus")
        if (selfId == callerId) {
            VideoCallStore.shared.hangup(null)
            finishAndRemoveTask()
            return
        }
        if (selfStatus == CallParticipantStatus.Waiting) {
            VideoCallStore.shared.reject(null)
        } else {
            VideoCallStore.shared.hangup(null)
        }
    }

    private fun initView() {
        calleeInfoView = findViewById(R.id.call_user_info)
        callEndHintView = findViewById(R.id.tv_call_end_hint)
        imageFloatButton = findViewById(R.id.image_float_icon)
        imageFloatButton?.setOnClickListener {
            if (FloatWindowManager.sharedInstance().isPictureInPictureSupported()) {
                enterPictureInPictureModeWithBuild()
            }
        }
        showCallStartedHint(getString(R.string.videochat_call_started_cost_hint))
        setBackground()
        addVideoCallView()
    }

    private fun openMediaDevice() {
        DeviceStore.shared().openLocalCamera(true, null)
        DeviceStore.shared().openLocalMicrophone(null)
        DeviceStore.shared().setAudioRoute(AudioRoute.SPEAKERPHONE)
    }

    private fun addVideoCallView() {
        val callViewContainer = findViewById<FrameLayout>(R.id.call_view_container)
        callViewContainer?.removeAllViews()
        videoCallView?.removeAllViews()
        videoCallView = CallView(this)
        videoCallView?.setLayoutTemplate(CallLayoutTemplate.Float)
        callViewContainer?.addView(videoCallView)
        callViewContainer.setOnClickListener {
            videoCallView?.showFunctionView(hideFunctionView)
            hideFunctionView = !hideFunctionView
        }
    }

    private fun enterPictureInPictureModeWithBuild() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            return
        }
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.O && hasPipModePermission()) {
            val pictureInPictureParams: PictureInPictureParams.Builder = PictureInPictureParams.Builder()
            val floatViewWidth = resources.getDimensionPixelSize(R.dimen.video_call_small_view_width)
            val floatViewHeight = resources.getDimensionPixelSize(R.dimen.video_call_small_view_height)
            val aspectRatio = Rational(floatViewWidth, floatViewHeight)
            pictureInPictureParams.setAspectRatio(aspectRatio).build()
            val requestPipSuccess = this.enterPictureInPictureMode(pictureInPictureParams.build())
            if (!requestPipSuccess) {
                return
            }
        } else {
            Log.w(TAG, "current version (" + Build.VERSION.SDK_INT + ") does not support picture-in-picture")
        }
    }

    private fun hasPipModePermission(): Boolean {
        val appOpsManager = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val hasPipModePermission =
            (AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                this.applicationInfo.uid,
                this.packageName
            ))
        if (!hasPipModePermission) {
            VideoChatToast.show(getString(R.string.videochat_permission_enter_pip_fail))
        }
        return hasPipModePermission
    }

    private fun setBackground() {
        val imageBackground = findViewById<ImageView>(R.id.img_view_background)
        val selfUser = CallStore.shared.observerState.selfInfo.value
        val option =
            ImageOptions.Builder().setPlaceImage(R.drawable.videochat_ic_default_avatar).setBlurEffect(80f).build()
        ImageLoader.load(this, imageBackground, selfUser.avatarURL, option)
        imageBackground?.setColorFilter(ContextCompat.getColor(this, R.color.videochat_color_blur_mask))
    }

    private fun handleCallEnded(reason: CallEndReason, userId: String) {
        val endHintText = getEndCallHintText(reason, userId)
        if (endHintText.isNullOrEmpty()) {
            finishAndRemoveTask()
            return
        }
        showEndCallHint(endHintText)
    }

    private fun showEndCallHint(text: String) {
        finishActivityJob?.cancel()
        callEndHintView?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            this.text = text
        }
        finishActivityJob = lifecycleScope.launch {
            delay(CALL_END_HINT_DURATION_MS)
            finishAndRemoveTask()
        }
    }

    private fun showCallStartedHint(text: String) {
        callEndHintView?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            this.text = text
        }
        lifecycleScope.launch {
            delay(CALL_STARTED_HINT_DURATION_MS)
            callEndHintView?.visibility = View.GONE
        }
    }

    private fun getEndCallHintText(reason: CallEndReason, userId: String): String? {
        val activeCall = CallStore.shared.observerState.activeCall.value
        val selfInfo = CallStore.shared.observerState.selfInfo.value
        if (activeCall.inviteeIds.size > 1 || activeCall.chatGroupId.isNotEmpty() || selfInfo.id == userId) {
            return null
        }
        return when (reason) {
            CallEndReason.Hangup -> getString(R.string.videochat_toast_other_party_hung_up)
            CallEndReason.Reject -> getString(R.string.videochat_toast_other_party_declined)
            CallEndReason.NoResponse -> getString(R.string.videochat_toast_other_party_no_response)
            CallEndReason.LineBusy -> getString(R.string.videochat_toast_other_party_busy)
            CallEndReason.Canceled -> getString(R.string.videochat_toast_other_party_cancelled)
            else -> null
        }
    }

    private fun setScreenLockParams(window: Window) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun applyWindowInsets() {
        val rootView = findViewById<View>(R.id.root_container)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }
    }

    companion object {
        private const val TAG = "VideoCallActivity"
        private const val CALL_END_HINT_DURATION_MS = 1000L
        private const val CALL_STARTED_HINT_DURATION_MS = 5500L

        /**
         * Weak reference to the currently alive VideoCallActivity.
         * The app layer can use this reference to get the Activity instance for showing dialogs (e.g. top-up prompt).
         */
        var currentInstance: WeakReference<VideoCallPage>? = null
            private set
    }
}