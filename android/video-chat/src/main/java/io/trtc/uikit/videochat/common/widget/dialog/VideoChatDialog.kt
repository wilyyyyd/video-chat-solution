package io.trtc.uikit.videochat.common.widget.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Themed confirmation dialog — pink-purple gradient confirm button + optional cancel button.
 *
 * Usage:
 * ```
 * VideoChatDialog(context)
 *     .setTitle("Log out")
 *     .setMessage("Are you sure you want to log out?")
 *     .setConfirmText("Confirm")
 *     .setCancelText("Cancel")
 *     .setOnConfirmListener { }
 *     .show()
 * ```
 */
class VideoChatDialog(context: Context) : Dialog(context, R.style.VideoChatDialogTheme) {

    private var title: String = ""
    private var message: String = ""
    private var confirmText: String = "Confirm"
    private var cancelText: String? = null
    private var onConfirmListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null

    fun setTitle(title: String): VideoChatDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): VideoChatDialog {
        this.message = message
        return this
    }

    fun setConfirmText(text: String): VideoChatDialog {
        this.confirmText = text
        return this
    }

    fun setCancelText(text: String): VideoChatDialog {
        this.cancelText = text
        return this
    }

    fun setOnConfirmListener(listener: () -> Unit): VideoChatDialog {
        this.onConfirmListener = listener
        return this
    }

    fun setOnCancelListener(listener: () -> Unit): VideoChatDialog {
        this.onCancelListener = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.videochat_layout_dialog)
        setupWindow()
        bindViews()
    }

    private fun setupWindow() {
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun bindViews() {
        bindTitle()
        bindMessage()
        bindConfirmButton()
        bindCancelButton()
    }

    private fun bindTitle() {
        findViewById<TextView>(R.id.tv_dialog_title)?.text = title
    }

    private fun bindMessage() {
        findViewById<TextView>(R.id.tv_dialog_message)?.text = message
    }

    private fun bindConfirmButton() {
        val button = findViewById<TextView>(R.id.btn_dialog_confirm) ?: return
        button.text = confirmText
        button.background = Theme.roundedGradient(dip2px(22f).toFloat())
        button.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
    }

    private fun bindCancelButton() {
        val button = findViewById<TextView>(R.id.btn_dialog_cancel) ?: return
        if (cancelText == null) {
            button.visibility = View.GONE
            return
        }
        button.text = cancelText
        button.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }
}
