package io.trtc.uikit.videochat.common.widget.background

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R

/**
 * Cover background layer — avatar image + top dark mask + bottom white gradient transition.
 * Bottom gradient transitions to white, blending smoothly with the info card's white transparent background.
 *
 * Shared component: used by both UserProfilePage and SettingsPage.
 */
class BackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageBackgroundView: ImageView

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, dip2px(360f))

        imageBackgroundView = ImageView(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        addView(imageBackgroundView)

        addView(View(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, dip2px(100f)).apply {
                gravity = Gravity.TOP
            }
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(context, R.color.videochat_overlay_dark_bg),
                    0x00000000
                )
            )
        })

        addView(View(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, dip2px(140f)).apply {
                gravity = Gravity.BOTTOM
            }
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    0x00FFFFFF,
                    ContextCompat.getColor(context, R.color.videochat_white_40),
                    ContextCompat.getColor(context, R.color.videochat_white_80),
                    ContextCompat.getColor(context, R.color.videochat_page_bg_light)
                )
            )
        })
    }

    internal fun setBackgroundUrl(pictureUrl: String?) {
        if (pictureUrl.isNullOrEmpty()) {
            Log.w(TAG, "pictureUrl is null or empty, using default avatar")
            imageBackgroundView.setImageResource(R.drawable.videochat_ic_default_avatar)
            return
        }
        Glide.with(context).load(pictureUrl).centerCrop().into(imageBackgroundView)
    }

    companion object {
        private const val TAG = "BackgroundView"
    }
}
