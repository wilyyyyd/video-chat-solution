package io.trtc.uikit.videochat.common.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.transition.TransitionManager
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMUserFullInfo.V2TIM_GENDER_FEMALE
import com.tencent.imsdk.v2.V2TIMUserFullInfo.V2TIM_GENDER_MALE
import io.trtc.tuikit.atomicxcore.api.login.Gender
import io.trtc.tuikit.atomicxcore.api.login.UserProfile
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.videochat.databinding.VideochatLayoutUserInfoCardBinding

/**
 * 用户信息卡片组件 — 渐变光环头像 + 昵称 + 统计 + 标签 + 可选签名展开/收起。
 *
 * 通用可复用：UserProfilePage 和 SettingsPage 共用。
 *
 * 对外 API：
 * - [bind] 绑定用户基本信息（可选显示签名）
 * - [setStats] 更新关注/粉丝/获赞数
 */
class UserInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = VideochatLayoutUserInfoCardBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var isExpanded = false

    init {
        clipChildren = false
        clipToPadding = false
        binding.tvToggle.setOnClickListener { toggleExpand() }
    }

    internal fun bind(userInfo: UserProfile, showSignature: Boolean = false) {
        val userName = userInfo.nickname ?: userInfo.userID
        with(binding) {
            tvName.text = userName
            Theme.applyGradientText(tvName)
            tvUserId.text = context.getString(R.string.videochat_user_id_format, userInfo.userID)
            tvUserId.setOnClickListener {
                copyToClipboard(userInfo.userID)
            }
            avatarView.loadAvatar(userInfo.avatarURL)
            layoutTags.removeAllViews()
            addGenderTag(userInfo.gender)
            addTag(context.getString(R.string.videochat_age_format, userInfo.birthday))

            if (showSignature) {
                val signature = userInfo.selfSignature?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.videochat_default_signature)
                setupSignature(signature)
            } else {
                divider.visibility = View.GONE
                tvSignature.visibility = View.GONE
                tvToggle.visibility = View.GONE
            }
        }
    }

    internal fun setStats(following: Long, followers: Long, likes: Long) {
        val ctx = context
        val text = "${formatCount(following)}${ctx.getString(R.string.videochat_stat_following)}" +
                "·${formatCount(followers)}${ctx.getString(R.string.videochat_stat_followers)}" +
                "·${formatCount(likes)}${ctx.getString(R.string.videochat_stat_likes)}"
        binding.tvStats.text = text
    }

    private fun setupSignature(signature: String) {
        with(binding) {
            divider.visibility = View.VISIBLE
            tvSignature.visibility = View.VISIBLE
            tvSignature.text = signature
            tvSignature.maxLines = 2
            tvSignature.ellipsize = TextUtils.TruncateAt.END
            isExpanded = false
            tvToggle.text = context.getString(R.string.videochat_expand)
            tvToggle.visibility = View.GONE
            checkNeedToggle()
        }
    }

    private fun checkNeedToggle() {
        binding.tvSignature.post {
            val tv = binding.tvSignature
            val overflow = tv.lineCount > 2 || tv.layout?.let {
                it.getEllipsisCount(it.lineCount - 1) > 0
            } == true
            if (overflow) binding.tvToggle.visibility = View.VISIBLE
        }
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded
        TransitionManager.beginDelayedTransition(binding.cardLayout)
        with(binding.tvSignature) {
            maxLines = if (isExpanded) Int.MAX_VALUE else 2
            ellipsize = if (isExpanded) null else TextUtils.TruncateAt.END
        }
        binding.tvToggle.text = context.getString(
            if (isExpanded) R.string.videochat_collapse else R.string.videochat_expand
        )
    }

    private fun copyToClipboard(userId: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("user_id", userId)
        clipboard.setPrimaryClip(clip)
        VideoChatToast.show(context.getString(R.string.videochat_user_id_copied))
    }

    private fun addGenderTag(gender: Gender?) {
        val label = when (gender) {
            Gender.MALE -> context.getString(R.string.videochat_gender_male)
            Gender.FEMALE -> context.getString(R.string.videochat_gender_female)
            else -> context.getString(R.string.videochat_gender_secret)
        }
        addTag(label)
    }

    private fun addTag(label: String) {
        val tagView = TagView(context, label)
        binding.layoutTags.addView(tagView, tagView.defaultLayoutParams())
    }
}

internal fun formatCount(count: Long): String = when {
    count >= 10000 -> {
        val result = "%.1f".format(count / 10000.0)
        "${result.removeSuffix(".0")}w"
    }
    count >= 1000 -> "%,d".format(count)
    count < 0 -> "0"
    else -> count.toString()
}
