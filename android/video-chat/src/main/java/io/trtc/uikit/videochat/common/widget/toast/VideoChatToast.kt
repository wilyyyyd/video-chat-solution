package io.trtc.uikit.videochat.common.widget.toast

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.tencent.cloud.tuikit.engine.common.ContextProvider
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Pink-purple gradient style Toast — centered on screen.
 *
 * Usage:
 * ```
 * VideoChatToast.show("Operation successful")
 * ```
 */
object VideoChatToast {

    fun show(message: String) {
        val context = ContextProvider.getApplicationContext()
        val toast = Toast(context)
        val view = inflateView(message)
        toast.view = view
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }

    fun showLong(message: String) {
        val context = ContextProvider.getApplicationContext()
        val toast = Toast(context)
        val view = inflateView(message)
        toast.view = view
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }

    private fun inflateView(message: String): TextView {
        val context = ContextProvider.getApplicationContext()
        val view = LayoutInflater.from(context)
            .inflate(R.layout.videochat_layout_toast, null) as TextView
        view.text = message
        view.background = Theme.roundedGradient(dip2px(20f).toFloat())
        return view
    }
}
