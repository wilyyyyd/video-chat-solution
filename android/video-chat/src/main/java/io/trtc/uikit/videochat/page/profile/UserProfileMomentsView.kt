package io.trtc.uikit.videochat.page.profile

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.databinding.VideochatLayoutProfileMomentsBinding

/**
 * Moments photo area — white rounded card + "Moments" title + 2×3 photo grid.
 *
 * Layout is declared by [videochat_layout_profile_moments.xml],
 * margins are controlled by the parent container via LayoutParams.
 */
class UserProfileMomentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VideochatLayoutProfileMomentsBinding.inflate(
        LayoutInflater.from(context), this
    )

    private val photoSlots: List<ImageView>

    init {
        orientation = VERTICAL
        val pad = dip2px(14f)
        setPadding(pad, pad, pad, pad)

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dip2px(20f).toFloat()
            setColor(ContextCompat.getColor(context, R.color.videochat_card_bg_white))
            setStroke(dip2px(1f), ContextCompat.getColor(context, R.color.videochat_card_stroke_light))
        }
        clipToOutline = true

        photoSlots = listOf(
            binding.ivPhoto0, binding.ivPhoto1, binding.ivPhoto2,
            binding.ivPhoto3, binding.ivPhoto4, binding.ivPhoto5
        )

        val cellRadius = dip2px(10f).toFloat()
        photoSlots.forEach {
            it.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = cellRadius
                setColor(ContextCompat.getColor(context, R.color.videochat_avatar_placeholder))
            }
            it.clipToOutline = true
        }
    }

    fun loadPhotos(urls: List<String>) {
        photoSlots.forEachIndexed { index, imageView ->
            val url = urls.getOrNull(index)
            if (url.isNullOrEmpty()) {
                imageView.visibility = View.INVISIBLE
                Glide.with(context).clear(imageView)
            } else {
                imageView.visibility = View.VISIBLE
                Glide.with(context).load(url).transform(CenterCrop()).into(imageView)
            }
        }
    }
}
