package io.trtc.uikit.demo.topup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.trtc.uikit.demo.R

/**
 * Grid Adapter for top-up coin options.
 * Supports single selection with highlighted pink-purple gradient border + twinkling star animation on the selected item.
 */
class CoinOptionAdapter(
    private val options: List<CoinOption>,
    private var selectedPosition: Int,
    private val onItemSelected: (Int) -> Unit,
) : RecyclerView.Adapter<CoinOptionAdapter.ViewHolder>() {

    companion object {
        // ============================================================
        // ✨ Star animation tunable parameters — adjust all animation behavior here
        // ============================================================

        const val SPARKLE_SELECTED_TEXT_SIZE = 14f

        const val SPARKLE_UNSELECTED_TEXT_SIZE = 12f

        const val SPARKLE_SELECTED_COLOR = 0xFFFFB800.toInt()

        const val SPARKLE_UNSELECTED_COLOR = 0xFFFFB800.toInt()

        const val SPARKLE_SELECTED_SCALE_MIN = 0.8f

        const val SPARKLE_SELECTED_SCALE_MAX = 1.6f

        const val SPARKLE_UNSELECTED_SCALE_MIN = 0.7f

        const val SPARKLE_UNSELECTED_SCALE_MAX = 1.3f

        const val SPARKLE_SELECTED_ALPHA_MIN = 0.6f

        const val SPARKLE_SELECTED_ALPHA_MAX = 1f

        const val SPARKLE_UNSELECTED_ALPHA_MIN = 0.4f

        const val SPARKLE_UNSELECTED_ALPHA_MAX = 0.9f

        const val SPARKLE_SELECTED_DURATION_MS = 1200L

        const val SPARKLE_UNSELECTED_DURATION_MS = 1800L

        const val SPARKLE_SELECTED_ROTATION_ENABLED = true

        const val SPARKLE_SELECTED_ROTATION_DEGREES = 360f
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.item_root)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_coin_amount)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_coin_desc)
        val tvSparkle: TextView = itemView.findViewById(R.id.tv_sparkle)
        var sparkleAnimator: AnimatorSet? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item_topup_coin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.tvAmount.text = "${option.amount}"
        holder.tvDesc.text = option.description

        val isSelected = position == selectedPosition
        holder.root.isSelected = isSelected

        if (isSelected) {
            applyGradientText(holder.tvAmount)
        } else {
            holder.tvAmount.paint.shader = null
            holder.tvAmount.setTextColor(0xFF1F2937.toInt())
        }

        startSparkleAnimation(holder, isSelected)

        holder.root.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onItemSelected(selectedPosition)
        }
    }

    override fun getItemCount(): Int = options.size

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.sparkleAnimator?.cancel()
        holder.sparkleAnimator = null
    }

    private fun applyGradientText(tv: TextView) {
        tv.post {
            val width = tv.paint.measureText(tv.text.toString())
            tv.paint.shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(0xFFFF2D78.toInt(), 0xFFB44FFF.toInt()),
                null, Shader.TileMode.CLAMP
            )
            tv.invalidate()
        }
    }

    private fun startSparkleAnimation(holder: ViewHolder, isSelected: Boolean) {
        holder.tvSparkle.visibility = View.VISIBLE
        holder.sparkleAnimator?.cancel()

        val scaleMin = if (isSelected) SPARKLE_SELECTED_SCALE_MIN else SPARKLE_UNSELECTED_SCALE_MIN
        val scaleMax = if (isSelected) SPARKLE_SELECTED_SCALE_MAX else SPARKLE_UNSELECTED_SCALE_MAX
        val alphaMin = if (isSelected) SPARKLE_SELECTED_ALPHA_MIN else SPARKLE_UNSELECTED_ALPHA_MIN
        val alphaMax = if (isSelected) SPARKLE_SELECTED_ALPHA_MAX else SPARKLE_UNSELECTED_ALPHA_MAX
        val duration = if (isSelected) SPARKLE_SELECTED_DURATION_MS else SPARKLE_UNSELECTED_DURATION_MS

        holder.tvSparkle.setTextColor(if (isSelected) SPARKLE_SELECTED_COLOR else SPARKLE_UNSELECTED_COLOR)
        holder.tvSparkle.textSize = if (isSelected) SPARKLE_SELECTED_TEXT_SIZE else SPARKLE_UNSELECTED_TEXT_SIZE

        val scaleX = ObjectAnimator.ofFloat(holder.tvSparkle, View.SCALE_X, scaleMin, scaleMax, scaleMin).apply {
            repeatCount = ObjectAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(holder.tvSparkle, View.SCALE_Y, scaleMin, scaleMax, scaleMin).apply {
            repeatCount = ObjectAnimator.INFINITE
        }
        val alpha = ObjectAnimator.ofFloat(holder.tvSparkle, View.ALPHA, alphaMin, alphaMax, alphaMin).apply {
            repeatCount = ObjectAnimator.INFINITE
        }

        val animators = mutableListOf(scaleX, scaleY, alpha)

        if (isSelected && SPARKLE_SELECTED_ROTATION_ENABLED) {
            val rotation = ObjectAnimator.ofFloat(
                holder.tvSparkle, View.ROTATION, 0f, SPARKLE_SELECTED_ROTATION_DEGREES
            ).apply {
                repeatCount = ObjectAnimator.INFINITE
            }
            animators.add(rotation)
        } else {
            holder.tvSparkle.rotation = 0f
        }

        holder.sparkleAnimator = AnimatorSet().apply {
            playTogether(animators.toList())
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
