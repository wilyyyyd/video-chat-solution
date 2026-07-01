package io.trtc.uikit.videochat.common.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Unified tag component supporting multiple visual styles.
 *
 * Usage:
 * ```
 * val tag = TagView(context, "Travel")                    // Default purple stroke
 * val overlay = TagView(context, "Beijing", TagView.Style.OVERLAY)  // White text, semi-transparent
 * container.addView(tag, tag.defaultLayoutParams())
 * ```
 */
class TagView(
    context: Context,
    label: String,
    style: Style = Style.OUTLINED
) : TextView(context) {

    enum class Style {
        OUTLINED,
        SMALL_OUTLINED,
        FROSTED,
        OVERLAY
    }

    init {
        text = label
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        maxWidth = dip2px(120f)
        when (style) {
            Style.OUTLINED -> applyOutlined()
            Style.SMALL_OUTLINED -> applySmallOutlined()
            Style.FROSTED -> applyFrosted()
            Style.OVERLAY -> applyOverlay()
        }
    }

    private fun applyOutlined() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(Theme.DEEP_PURPLE)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dip2px(12f).toFloat()
            setColor(Theme.BG_TAG_PURPLE)
            setStroke(dip2px(1f), Theme.PURPLE)
        }
        setPadding(dip2px(10f), dip2px(4f), dip2px(10f), dip2px(4f))
    }

    /** For list layout: lighter background + light purple stroke + medium purple text + small size */
    private fun applySmallOutlined() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTextColor(Theme.TEXT_TAG_LIGHT)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dip2px(10f).toFloat()
            setColor(Theme.BG_TAG_PURPLE_LIGHT)
            setStroke(dip2px(1f), Theme.STROKE_TAG_LIGHT)
        }
        setPadding(dip2px(8f), dip2px(3f), dip2px(8f), dip2px(3f))
    }

    private fun applyFrosted() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
        setTextColor(Theme.TEXT_WHITE)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dip2px(8f).toFloat()
            setColor(Theme.BG_TAG_FROSTED)
        }
        setPadding(dip2px(7f), dip2px(3f), dip2px(7f), dip2px(3f))
    }

    private fun applyOverlay() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
        setTextColor(ContextCompat.getColor(context, R.color.videochat_color_white))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dip2px(8f).toFloat()
            setColor(ContextCompat.getColor(context, R.color.videochat_tag_overlay_bg))
        }
        setPadding(dip2px(6f), dip2px(2f), dip2px(6f), dip2px(2f))
    }

    fun defaultLayoutParams(): MarginLayoutParams {
        return MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            marginEnd = dip2px(6f)
            bottomMargin = dip2px(6f)
        }
    }
}
