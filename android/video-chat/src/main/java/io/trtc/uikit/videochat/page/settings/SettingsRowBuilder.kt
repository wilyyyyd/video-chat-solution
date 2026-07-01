package io.trtc.uikit.videochat.page.settings

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Settings row View builder for SettingsPage.
 *
 * All methods are Context extension functions, called via `requireContext().xxx()` in a Fragment.
 */

internal fun Context.dividerView(): View {
    return View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dip2px(1f)
        )
        setBackgroundColor(Theme.BG_DIVIDER)
    }
}

internal fun Context.buildSettingsRow(
    label: String,
    iconRes: Int,
    onClick: (() -> Unit)? = null
): View {
    val row = createSettingsRowBase()
    row.setOnClickListener { onClick?.invoke() }

    row.addView(ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dip2px(18f), dip2px(18f)).apply { marginEnd = dip2px(10f) }
        setImageResource(iconRes)
        setColorFilter(Theme.PINK)
    })
    row.addView(settingsLabel(label))
    row.addView(settingsChevron())

    return row
}

internal fun Context.buildOnlineStatusRow(
    checked: Boolean,
    onToggleChanged: ((Boolean) -> Unit)? = null
): View {
    val row = createSettingsRowBase()

    row.addView(View(this).apply {
        layoutParams = LinearLayout.LayoutParams(dip2px(10f), dip2px(10f)).apply { marginEnd = dip2px(10f) }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Theme.BG_ONLINE_GREEN)
        }
    })
    row.addView(settingsLabel(getString(R.string.videochat_online_status)))

    val switch = SwitchCompat(this).apply {
        isChecked = checked
        thumbDrawable = AppCompatResources.getDrawable(context, R.drawable.videochat_switch_thumb)
        trackDrawable = AppCompatResources.getDrawable(context, R.drawable.videochat_switch_track)
        showText = false
        onToggleChanged?.let { listener ->
            setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
        }
    }
    row.addView(switch)
    return row
}

private fun Context.settingsLabel(text: String): TextView {
    return TextView(this).apply {
        this.text = text
        setTextColor(Theme.TEXT_SECONDARY)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        layoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
    }
}

private fun Context.settingsChevron(): ImageView {
    return ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dip2px(16f), dip2px(16f))
        setImageResource(R.drawable.videochat_ic_return_arrow)
        setColorFilter(Theme.TEXT_HINT)
    }
}

private fun Context.createSettingsRowBase(): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dip2px(16f), dip2px(14f), dip2px(16f), dip2px(14f))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
}
