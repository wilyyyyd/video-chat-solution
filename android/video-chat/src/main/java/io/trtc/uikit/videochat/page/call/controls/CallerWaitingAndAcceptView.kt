package io.trtc.uikit.videochat.page.call.controls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.tencent.liteav.base.Log
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.utils.PermissionRequest
import io.trtc.uikit.videochat.common.widget.button.ControlButton
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.videochat.page.beauty.BeautyIntegration
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.tuikit.common.permission.PermissionCallback
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.device.AudioRoute
import io.trtc.tuikit.atomicxcore.api.device.DeviceStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class CallerWaitingAndAcceptView(context: Context) : RelativeLayout(context) {
    private var subscribeStateJob: Job? = null

    private lateinit var buttonHangup: ControlButton
    private lateinit var buttonAudioRoute: ControlButton
    private lateinit var buttonCamera: ControlButton
    private lateinit var buttonMicrophone: ControlButton

    private lateinit var buttonBeauty: ControlButton
    private lateinit var buttonGift: ControlButton

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.layoutParams?.width = LayoutParams.MATCH_PARENT
        this.layoutParams?.height = LayoutParams.MATCH_PARENT
        initView()
        registerObserver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscribeStateJob?.cancel()
    }

    private fun registerObserver() {
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            supervisorScope {
                launch {
                    DeviceStore.shared().deviceState.cameraStatus.collect {
                        updateCameraButton(it)
                    }
                }
                launch {
                    DeviceStore.shared().deviceState.microphoneStatus.collect {
                        updateMicrophoneButton(it)
                    }
                }
                launch {
                    DeviceStore.shared().deviceState.currentAudioRoute.collect {
                        updateAudioRouteButton(it)
                    }
                }
            }
        }
    }

    private fun updateCameraButton(cameraStatus: DeviceStatus) {
        val cameraIsOpened = (cameraStatus == DeviceStatus.ON)
        buttonCamera.imageView.isActivated = cameraIsOpened
        buttonCamera.textView.text = when {
            cameraIsOpened -> context.getString(R.string.videochat_text_enable_camera)
            else -> context.getString(R.string.videochat_text_disable_camera)
        }
    }

    private fun updateMicrophoneButton(microphoneStatus: DeviceStatus) {
        val isMute = microphoneStatus == DeviceStatus.OFF
        val resId = if (isMute) {
            R.string.videochat_text_enable_mute
        } else {
            R.string.videochat_text_disable_mute
        }
        buttonMicrophone.textView.text = context.getString(resId)
        buttonMicrophone.imageView.isActivated = !isMute
    }

    private fun updateAudioRouteButton(audioRoute: AudioRoute) {
        val isSpeaker = audioRoute == AudioRoute.SPEAKERPHONE
        val resId = if (isSpeaker) R.string.videochat_text_speaker else R.string.videochat_text_earpiece
        buttonAudioRoute.textView.text = context.getString(resId)
        buttonAudioRoute.imageView.isActivated = isSpeaker
        buttonAudioRoute.imageView.setImageResource(R.drawable.videochat_bg_audio_device)
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.videochat_function_view_video_inviting, this)
        buttonHangup = findViewById(R.id.cb_hangup)
        buttonCamera = findViewById(R.id.cb_camera)
        buttonAudioRoute = findViewById(R.id.cb_audio_route)
        buttonMicrophone = findViewById(R.id.cb_microphone)
        buttonGift = findViewById(R.id.cb_gift)
        buttonBeauty = findViewById(R.id.cb_beauty)
        updateMicrophoneButton(DeviceStore.shared().deviceState.microphoneStatus.value)
        updateCameraButton(DeviceStore.shared().deviceState.cameraStatus.value)
        updateAudioRouteButton(DeviceStore.shared().deviceState.currentAudioRoute.value)
        initViewListener()
    }

    private fun initViewListener() {
        buttonHangup.setOnClickListener {
            handleHangup()
        }
        buttonMicrophone.setOnClickListener {
            if (!buttonMicrophone.isEnabled) {
                return@setOnClickListener
            }
            toggleMicrophone()
        }
        buttonAudioRoute.setOnClickListener {
            if (!buttonAudioRoute.isEnabled) {
                return@setOnClickListener
            }
            toggleAudioRoute()
        }
        buttonCamera.setOnClickListener {
            if (!buttonCamera.isEnabled) {
                return@setOnClickListener
            }
            toggleCamera()
        }
        buttonGift.setOnClickListener {
            VideoChatToast.show(context.getString(R.string.videochat_wip_toast))
        }
        buttonBeauty.setOnClickListener {
            BeautyIntegration.showBeautyDialog(context)
        }
    }

    private fun disableButton(button: View) {
        button.isEnabled = false
        button.alpha = 0.8f
    }

    private fun handleHangup() {
        buttonHangup.imageView.roundPercent = 1.0f
        buttonHangup.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.videochat_button_bg_red))
        ImageLoader.loadGif(context, buttonHangup.imageView, R.drawable.videochat_hangup_loading)
        disableButton(buttonCamera)
        disableButton(buttonAudioRoute)
        disableButton(buttonMicrophone)
        disableButton(buttonGift)
        disableButton(buttonBeauty)
        CallStore.shared.hangup(null)
    }

    private fun toggleMicrophone() {
        val isMicrophoneOpen = (DeviceStore.shared().deviceState.microphoneStatus.value == DeviceStatus.ON)
        if (isMicrophoneOpen) {
            DeviceStore.shared().closeLocalMicrophone()
        } else {
            DeviceStore.shared().openLocalMicrophone(null)
        }
    }

    private fun toggleAudioRoute() {
        val currentAudioRoute = DeviceStore.shared().deviceState.currentAudioRoute.value
        if (currentAudioRoute == AudioRoute.SPEAKERPHONE) {
            DeviceStore.shared().setAudioRoute(AudioRoute.EARPIECE)
        } else {
            DeviceStore.shared().setAudioRoute(AudioRoute.SPEAKERPHONE)
        }
    }

    private fun toggleCamera() {
        val isCameraOpened = (DeviceStore.shared().deviceState.cameraStatus.value == DeviceStatus.ON)
        if (isCameraOpened) {
            DeviceStore.shared().closeLocalCamera()
            buttonCamera.textView.text = context.resources.getString(R.string.videochat_text_disable_camera)
        } else {
            openLocalCamera()
        }
    }

    private fun openLocalCamera() {
        PermissionRequest.requestPermissions(context, CallMediaType.Video, object : PermissionCallback() {
            override fun onGranted() {
                val isFrontCamera = DeviceStore.shared().deviceState.isFrontCamera.value
                DeviceStore.shared().openLocalCamera(isFrontCamera, null)
                buttonCamera.textView.text = context.resources.getString(R.string.videochat_text_enable_camera)
            }

            override fun onDenied() {
                Log.e("CallerWaitingAndAcceptView","openCamera failed, errMsg: camera permission denied")
            }
        })
    }
}