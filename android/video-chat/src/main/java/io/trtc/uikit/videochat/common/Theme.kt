package io.trtc.uikit.videochat.common

import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.widget.TextView

/**
 * Global theme configuration that centralizes color and gradient style management.
 *
 * Always reference constants here in View code to avoid hardcoding color values.
 */
object Theme {

    // ── Brand Colors ──
    const val PINK = 0xFFFF2D78.toInt()
    const val PURPLE = 0xFFB44FFF.toInt()
    const val DEEP_PURPLE = 0xFF7C3AED.toInt()

    val GRADIENT_PINK_PURPLE = intArrayOf(PINK, PURPLE)

    // ── Text Colors ──
    const val TEXT_WHITE = 0xFFFFFFFF.toInt()
    const val TEXT_WHITE_80 = 0xCCFFFFFF.toInt()
    const val TEXT_PRIMARY = 0xFF1F2937.toInt()
    const val TEXT_SECONDARY = 0xFF374151.toInt()
    const val TEXT_HINT = 0xFF9CA3AF.toInt()
    const val TEXT_DANGER = 0xFFEF4444.toInt()

    // ── Background Colors ──
    const val BG_PAGE_TOP = 0xFFFDF2F8.toInt()
    const val BG_PAGE_MID = 0xFFFAF5FF.toInt()
    const val BG_PAGE_BOTTOM = 0xFFEFF6FF.toInt()
    const val BG_CARD_WHITE = 0xF2FFFFFF.toInt()
    const val BG_SETTINGS_CARD = 0xF5FFFFFF.toInt()
    const val BG_TOGGLE_OFF = 0xFFD1D5DB.toInt()
    const val BG_ONLINE_GREEN = 0xFF22C55E.toInt()
    const val BG_DIVIDER = 0x0D000000.toInt()
    const val BG_DANGER_BUTTON = 0xE6FEE2E2.toInt()
    const val BG_TAG_PURPLE = 0xFFF3E8FF.toInt()
    const val BG_TAG_PURPLE_LIGHT = 0xFFF9F0FF.toInt()
    const val STROKE_TAG_LIGHT = 0xFFD8B4FE.toInt()
    const val TEXT_TAG_LIGHT = 0xFFA855F7.toInt()
    const val BG_TAG_FROSTED = 0x40FFFFFF.toInt() // 25% white (same as unfollowed button)

    // ── Cover Overlay ──
    const val COVER_OVERLAY_TOP = 0x4D000000.toInt()
    const val COVER_OVERLAY_MID = 0x1A000000.toInt()
    const val COVER_OVERLAY_BOTTOM = 0x66000000.toInt()
    val COVER_OVERLAY_COLORS = intArrayOf(COVER_OVERLAY_TOP, COVER_OVERLAY_MID, COVER_OVERLAY_BOTTOM)
    const val COVER_TRANSITION_END = 0xCCFDF2F8.toInt()

    // ── Gradient Drawables ──

    /** Rounded pink-purple gradient Drawable */
    internal fun roundedGradient(cornerRadiusPx: Float): GradientDrawable {
        return horizontalGradient().apply { cornerRadius = cornerRadiusPx }
    }

    /** Horizontal pink-purple gradient */
    private fun horizontalGradient(): GradientDrawable {
        return GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, GRADIENT_PINK_PURPLE)
    }

    // ── Text Gradients ──

    /** Apply pink-purple horizontal gradient to a TextView */
    fun applyGradientText(tv: TextView) {
        tv.post {
            val width = tv.paint.measureText(tv.text.toString())
            tv.paint.shader = LinearGradient(
                0f, 0f, width, 0f,
                GRADIENT_PINK_PURPLE,
                null,
                Shader.TileMode.CLAMP
            )
            tv.invalidate()
        }
    }

    /** Switch tab text style */
    fun toggleTabText(tv: TextView, selected: Boolean) {
        if (selected) {
            applyGradientText(tv)
        } else {
            tv.paint.shader = null
            tv.setTextColor(TEXT_HINT)
        }
    }

    /** Tricolor vertical gradient page background (pink → purple → blue) */
    fun pageBackground(): PaintDrawable {
        return PaintDrawable().apply {
            shape = RectShape()
            shaderFactory = PageGradientFactory()
        }
    }

    private class PageGradientFactory : ShapeDrawable.ShaderFactory() {
        override fun resize(width: Int, height: Int): Shader {
            return LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(BG_PAGE_TOP, BG_PAGE_MID, BG_PAGE_BOTTOM),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }
}