package io.trtc.uikit.videochat.common.widget.button

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R

/**
 * Semi-transparent circular back button — white arrow + semi-transparent black background.
 *
 * Usage:
 * ```
 * root.addView(BackButton(this))
 * ```
 * Default click behavior calls finish() on the current Activity.
 */
class BackButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = LayoutParams(dip2px(44f), dip2px(44f)).apply {
            setMargins(dip2px(16f), dip2px(44f), 0, 0)
        }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(context, R.color.videochat_overlay_dark_bg))
        }

        val arrow = ImageView(context).apply {
            layoutParams = LayoutParams(dip2px(20f), dip2px(20f)).apply {
                gravity = Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.videochat_ic_return_arrow)
            setColorFilter(ContextCompat.getColor(context, R.color.videochat_color_white))
        }
        addView(arrow)

        setOnClickListener { (context as? Activity)?.finish() }
    }
}
