package io.trtc.uikit.videochat.common.widget.avatar

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Rounded-corner clipping OutlineProvider — clips View content by the specified corner radius.
 */
class RoundOutlineProvider(private val radiusPx: Float) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
    }
}
