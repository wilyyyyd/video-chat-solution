package io.trtc.uikit.videochat.page.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tencent.imsdk.v2.V2TIMUserFullInfo

import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.login.UserProfile
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.common.widget.UserInfoView
import io.trtc.uikit.videochat.common.widget.background.BackgroundView
import io.trtc.uikit.videochat.common.widget.dialog.VideoChatDialog
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.videochat.manager.UserInfoStore
import io.trtc.uikit.videochat.page.beauty.BeautyActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class SettingsPage : Fragment() {
    private lateinit var backgroundView: BackgroundView
    private lateinit var userInfoView: UserInfoView

    private val safeContext get() = requireContext()
    private var subscribeStateJob: Job? = null
    private val onlineStatusPrefs: SharedPreferences by lazy {
        safeContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "videochat_settings"
        private const val KEY_ONLINE_STATUS = "online_status_enabled"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return buildRootView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnlineStatus()
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            supervisorScope {
                launch { observeSelfFollowingUsers() }
                launch { observeSelfFollowers() }
                launch { observeSelfInfo() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscribeStateJob?.cancel()
    }

    private suspend fun observeSelfFollowingUsers() {
        UserInfoStore.shared.selfFollowingUsers.collect {
            val followersSize = UserInfoStore.shared.selfFollowers.value.size
            userInfoView.setStats(
                following = it.size.toLong(),
                followers = followersSize.toLong(),
                likes = 0
            )
        }
    }

    private suspend fun observeSelfFollowers() {
        UserInfoStore.shared.selfFollowers.collect {
            val followingSize = UserInfoStore.shared.selfFollowingUsers.value.size
            userInfoView.setStats(
                following = followingSize.toLong(),
                followers = it.size.toLong(),
                likes = 0
            )
        }
    }

    private suspend fun observeSelfInfo() {
        LoginStore.shared.loginState.loginUserInfo.collect {
            if (it == null) {
                return@collect
            }
            backgroundView.setBackgroundUrl(it.avatarURL)
            userInfoView.bind(it, showSignature = true)
        }
    }

    private fun buildRootView(): ScrollView {
        val scroll = ScrollView(safeContext).apply { setPadding(0, 0, 0, dip2px(16f)) }
        val root = android.widget.FrameLayout(safeContext).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            clipChildren = false
        }
        backgroundView = BackgroundView(safeContext)
        root.addView(backgroundView)
        root.addView(buildSettingsView())
        scroll.addView(root)
        return scroll
    }

    private fun buildSettingsView(): LinearLayout {
        val viewContainer = LinearLayout(safeContext).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
        }

        val topSpacerView = View(safeContext).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dip2px(220F))
        }
        viewContainer.addView(topSpacerView)

        userInfoView = UserInfoView(safeContext).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(dip2px(16f), 0, dip2px(16f), dip2px(8f))
            }
        }
        viewContainer.addView(userInfoView)
        viewContainer.addView(buildSettingsCard())
        return viewContainer
    }

    private fun buildSettingsCard(): View {
        val container = LinearLayout(safeContext).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                leftMargin = dip2px(16F); rightMargin = dip2px(16F); topMargin = dip2px(16F)
            }
        }
        container.addView(buildCard())
        container.addView(buildLogoutButton())
        return container
    }

    private fun buildCard(): LinearLayout {
        val card = LinearLayout(safeContext).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dip2px(16F).toFloat()
                setColor(Theme.BG_SETTINGS_CARD)
            }
        }
        val initialOnlineStatus = onlineStatusPrefs.getBoolean(KEY_ONLINE_STATUS, false)
        card.addView(safeContext.buildOnlineStatusRow(checked = initialOnlineStatus) { enabled ->
            setOnlineStatus(enabled)
        })
        card.addView(safeContext.dividerView())
        card.addView(safeContext.buildSettingsRow(getString(R.string.videochat_voice_changer), R.drawable.videochat_btn_voice_changer) {
            VideoChatToast.show(getString(R.string.videochat_wip_toast))
        })
        card.addView(safeContext.dividerView())
        card.addView(safeContext.buildSettingsRow(getString(R.string.videochat_beauty), R.drawable.videochat_ic_beauty) {
            startActivity(Intent(safeContext, BeautyActivity::class.java))
        })
        card.addView(safeContext.dividerView())
        card.addView(safeContext.buildSettingsRow(getString(R.string.videochat_my_gifts), R.drawable.videochat_btn_gift) {
            VideoChatToast.show(getString(R.string.videochat_wip_toast))
        })
        return card
    }

    private fun buildLogoutButton(): TextView {
        return TextView(safeContext).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dip2px(48f)).apply { topMargin = dip2px(16f) }
            text = getString(R.string.videochat_logout)
            setTextColor(Theme.TEXT_DANGER)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dip2px(16f).toFloat()
                setColor(Theme.BG_DANGER_BUTTON)
            }
            setOnClickListener { showLogoutDialog() }
        }
    }

    private fun initOnlineStatus() {
        val enabled = onlineStatusPrefs.getBoolean(KEY_ONLINE_STATUS, false)
        setOnlineStatus(enabled, needUpdate = false)
    }

    private fun setOnlineStatus(enabled: Boolean, needUpdate: Boolean = true) {
        if (needUpdate) {
            onlineStatusPrefs.edit()?.putBoolean(KEY_ONLINE_STATUS, enabled)?.apply()
        }
    }

    private fun showLogoutDialog() {
        VideoChatDialog(safeContext)
            .setTitle(getString(R.string.videochat_logout_title))
            .setMessage(getString(R.string.videochat_logout_message))
            .setCancelText(getString(R.string.videochat_cancel))
            .setConfirmText(getString(R.string.videochat_confirm))
            .setOnConfirmListener {
                LoginStore.shared.logout(object : CompletionHandler {
                    override fun onSuccess() { navigateToLogin() }
                    override fun onFailure(code: Int, desc: String) {}
                })
            }
            .show()
    }

    private fun navigateToLogin() {
        val activity = requireActivity()
        try {
            val intent = Intent(activity, Class.forName("io.trtc.uikit.demo.login.LoginActivity"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        } catch (_: ClassNotFoundException) {
            activity.finish()
        }
    }

}
