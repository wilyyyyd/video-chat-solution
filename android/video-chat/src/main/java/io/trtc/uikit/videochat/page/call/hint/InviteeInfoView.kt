package io.trtc.uikit.videochat.page.call.hint

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.manager.UserInfoStore
import io.trtc.uikit.videochat.common.widget.avatar.AvatarView
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Call page top-left user info pill: avatar + follow button.
 *
 * Usage: view.bind(remoteUserId, lifecycleOwner)
 */
class InviteeInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val avatar: AvatarView
    private val btnFollow: TextView
    private var subscribeStateJob: Job? = null

    init {
        inflate(context, R.layout.videochat_view_call_user_info, this)
        avatar = findViewById(R.id.iv_avatar)
        btnFollow = findViewById(R.id.btn_follow)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initView()
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            supervisorScope {
                launch { observeFellowStatus() }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscribeStateJob?.cancel()
    }

    private suspend fun observeFellowStatus() {
        val inviteeId = CallStore.shared.observerState.activeCall.value.inviteeIds.first()
        UserInfoStore.shared.selfFollowingUsers.collect {
            updateFollowButtonView(inviteeId)
        }
    }

    private fun initView() {
        val remoteUserId = getRemoteUserId()
        updateFollowButtonView(remoteUserId)
        UserInfoStore.shared.getUserProfile(remoteUserId,
            onResult = { profile ->
                if (profile == null) {
                    return@getUserProfile
                }
                setUserAvatar(remoteUserId, profile.faceUrl)
            },
            onError = {}
        )
        btnFollow.setOnClickListener {
            if (UserInfoStore.shared.isFollowing(remoteUserId)) {
                UserInfoStore.shared.unFellowUsers(listOf(remoteUserId))
            } else {
                UserInfoStore.shared.fellowUser(listOf(remoteUserId))
            }
        }
    }

    private fun setUserAvatar(userId: String, avatarURL: String) {
        avatar.loadAvatar(avatarURL)
        val followed = UserInfoStore.shared.isFollowing(userId)
        btnFollow.text = context.getString(if (followed) R.string.videochat_followed else R.string.videochat_follow)
        btnFollow.setBackgroundResource(if (followed) R.drawable.videochat_bg_btn_followed else R.drawable.videochat_bg_btn_follow)
    }

    private fun updateFollowButtonView(userId: String) {
        val followed = UserInfoStore.shared.isFollowing(userId)
        btnFollow.text = context.getString(
            if (followed) R.string.videochat_followed else R.string.videochat_follow
        )
        btnFollow.setBackgroundResource(
            if (followed) R.drawable.videochat_bg_btn_followed else R.drawable.videochat_bg_btn_follow
        )
    }

    private fun getRemoteUserId(): String {
        val inviteeId = CallStore.shared.observerState.activeCall.value.inviteeIds.first()
        val inviterId = CallStore.shared.observerState.activeCall.value.inviterId
        val selfId = CallStore.shared.observerState.selfInfo.value.id
        return if (selfId == inviterId) inviteeId else inviterId
    }
}
