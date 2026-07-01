package io.trtc.uikit.videochat.common.widget.panel

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Bottom sliding panel — slides in from bottom, top-rounded white card + pink-purple gradient drag handle.
 *
 * Usage:
 * ```
 * VideoChatBottomPanel(context)
 *     .setContent(myView)
 *     .show()
 * ```
 */
open class VideoChatBottomPanel(context: Context) : Dialog(context, R.style.VideoChatBottomPanelTheme) {

    private var panelContentView: View? = null
    private lateinit var panel: LinearLayout

    fun setContent(view: View): VideoChatBottomPanel {
        panelContentView = view
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener { dismiss() }
        }

        panel = buildPanel()
        val panelLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
        root.addView(panel, panelLp)
        setContentView(root)
        setupWindow()
    }

    private fun buildPanel(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, R.color.videochat_panel_bg_dark))
                cornerRadii = floatArrayOf(
                    dip2px(16f).toFloat(), dip2px(16f).toFloat(),
                    dip2px(16f).toFloat(), dip2px(16f).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
            clipToOutline = true

            addView(createHandle())
            panelContentView?.let { view ->
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(view, lp)
            }
        }
    }

    private fun createHandle(): View {
        return FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dip2px(16f)
            )
            // Centered pink-purple gradient pill
            val pill = View(context).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    Theme.GRADIENT_PINK_PURPLE
                ).apply {
                    cornerRadius = dip2px(2f).toFloat()
                }
            }
            val pillLp = FrameLayout.LayoutParams(dip2px(36f), dip2px(4f)).apply {
                gravity = Gravity.CENTER
            }
            addView(pill, pillLp)
        }
    }

    private fun setupWindow() {
        window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setGravity(Gravity.BOTTOM)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        setCanceledOnTouchOutside(true)
    }

    override fun show() {
        super.show()
        // Slide-in from bottom animation
        panel.post {
            panel.translationY = panel.height.toFloat()
            panel.animate()
                .translationY(0f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun dismiss() {
        if (!isShowing) {
            super.dismiss()
            return
        }
        panel.animate()
            .translationY(panel.height.toFloat())
            .setDuration(150)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { super.dismiss() }
            .start()
    }
}
