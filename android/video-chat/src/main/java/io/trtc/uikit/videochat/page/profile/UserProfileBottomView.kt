package io.trtc.uikit.videochat.page.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.databinding.VideochatLayoutProfileBottomViewBinding

/**
 * Profile page bottom action bar: Chat (breathing pulse) / Call / Follow (heart icon).
 * Background is a light blue-purple gradient with top rounded corners, blending with the overall app design.
 */
class UserProfileBottomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: VideochatLayoutProfileBottomViewBinding

    internal var onChatClick: (() -> Unit)? = null
    internal var onCallClick: (() -> Unit)? = null
    internal var onFollowClick: (() -> Unit)? = null

    private var pulseAnimator: AnimatorSet? = null
    private val shakeX = dip2px(2f).toFloat()

    private val pulseRunnable = object : Runnable {
        override fun run() {
            performPulse()
            postDelayed(this, 1500L)
        }
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val pad = dip2px(16f)
        setPadding(pad, pad, pad, dip2px(32f))
        setBackgroundResource(R.drawable.videochat_bg_profile_bottom_bar)
        clipChildren = false
        clipToPadding = false

        binding = VideochatLayoutProfileBottomViewBinding.inflate(
            LayoutInflater.from(context), this
        )

        with(binding) {
            btnChat.setOnClickListener { onChatClick?.invoke() }
            btnCall.setOnClickListener { onCallClick?.invoke() }
            btnFollow.setOnClickListener { onFollowClick?.invoke() }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(pulseRunnable, 500L)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(pulseRunnable)
        pulseAnimator?.cancel()
        pulseAnimator = null
        binding.btnChat.scaleX = 1f
        binding.btnChat.scaleY = 1f
        binding.ivChatIcon.translationX = 0f
    }

    fun setFollowed(followed: Boolean) {
        binding.ivFollowIcon.setFilled(followed)
    }

    /**
     * Breathing pulse animation:
     * - Chat button scaleX/Y: 1.0 → 1.05 → 1.0
     * - Icon translationX ±2dp fast horizontal shift
     * - Overall 600ms, interval 1.5s
     */
    private fun performPulse() {
        pulseAnimator?.cancel()

        val scaleX = ObjectAnimator.ofFloat(binding.btnChat, View.SCALE_X, 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.btnChat, View.SCALE_Y, 1f, 1.05f, 1f)
        val iconShake = ObjectAnimator.ofFloat(
            binding.ivChatIcon, View.TRANSLATION_X,
            0f, -shakeX, shakeX, -shakeX, shakeX, 0f
        ).apply {
            duration = 400L
            startDelay = 100L
            interpolator = DecelerateInterpolator()
        }

        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, iconShake)
            duration = 600L
            interpolator = OvershootInterpolator(1.8f)
            start()
        }
    }
}
