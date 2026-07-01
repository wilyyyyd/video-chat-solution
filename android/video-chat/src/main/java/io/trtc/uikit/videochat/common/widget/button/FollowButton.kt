package io.trtc.uikit.videochat.common.widget.button

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Custom-drawn heart icon supporting two states:
 * - Unfollowed: purple stroke outline heart
 * - Followed: pink solid filled heart
 *
 * Usage:
 * ```kotlin
 * val heart = FollowButton(context).apply {
 *     layoutParams = LayoutParams(dp(32), dp(32))
 * }
 * heart.setFilled(true)  // Switch to followed
 * ```
 */
class FollowButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isFilled = false

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Theme.PURPLE
        style = Paint.Style.STROKE
        strokeWidth = dip2px(2f).toFloat()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.videochat_follow_btn_fill)
        style = Paint.Style.FILL
    }

    private val path = Path()

    fun setFilled(filled: Boolean) {
        if (isFilled == filled) return
        isFilled = filled
        invalidate()
    }

    fun isFilled(): Boolean = isFilled

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val padding = w * 0.06f

        path.reset()
        val cx = w / 2f
        val top = padding
        val bottom = h - padding

        path.moveTo(cx, bottom * 0.88f)
        // Left half of heart
        path.cubicTo(
            padding - w * 0.02f, h * 0.52f,
            padding + w * 0.02f, top,
            cx, top + h * 0.18f
        )
        // Right half of heart
        path.cubicTo(
            w - padding - w * 0.02f, top,
            w - padding + w * 0.02f, h * 0.52f,
            cx, bottom * 0.88f
        )
        path.close()

        if (isFilled) {
            canvas.drawPath(path, fillPaint)
        } else {
            canvas.drawPath(path, strokePaint)
        }
    }
}
