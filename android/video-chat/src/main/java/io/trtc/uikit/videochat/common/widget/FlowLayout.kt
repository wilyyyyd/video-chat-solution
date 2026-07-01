package io.trtc.uikit.videochat.common.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * Simple FlowLayout: child views are laid out horizontally, wrapping to next line when exceeding width.
 * Used for tag display areas.
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var currentLineWidth = 0
        var totalHeight = paddingTop
        var lineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (currentLineWidth + childWidth > maxWidth) {
                totalHeight += lineHeight
                currentLineWidth = childWidth
                lineHeight = childHeight
            } else {
                currentLineWidth += childWidth
                lineHeight = maxOf(lineHeight, childHeight)
            }
        }
        totalHeight += lineHeight + paddingBottom

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            resolveSize(totalHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxWidth = width - paddingLeft - paddingRight
        var currentX = paddingLeft
        var currentY = paddingTop
        var lineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (currentX - paddingLeft + childWidth > maxWidth) {
                // Line break
                currentX = paddingLeft
                currentY += lineHeight
                lineHeight = 0
            }

            child.layout(
                currentX + lp.leftMargin,
                currentY + lp.topMargin,
                currentX + lp.leftMargin + child.measuredWidth,
                currentY + lp.topMargin + child.measuredHeight
            )
            currentX += childWidth
            lineHeight = maxOf(lineHeight, childHeight)
        }
    }

    override fun generateLayoutParams(attrs: android.util.AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }
}
