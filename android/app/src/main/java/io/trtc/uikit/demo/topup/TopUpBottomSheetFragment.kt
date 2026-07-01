package io.trtc.uikit.demo.topup

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.demo.R

/**
 * Bottom sheet for top-up when running low on coins during a call.
 *
 * Usage:
 * ```
 * TopUpBottomSheetFragment.newInstance().show(supportFragmentManager, "topup")
 * ```
 */
class TopUpBottomSheetFragment : BottomSheetDialogFragment() {

    private var selectedPosition = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            bottomSheet?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.app_fragment_topup_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCoinGrid(view)
        setupPayButton(view)
    }

    private fun setupCoinGrid(view: View) {
        val coinOptions = listOf(
            CoinOption(50, getString(R.string.app_topup_coin_50)),
            CoinOption(100, getString(R.string.app_topup_coin_100)),
            CoinOption(150, getString(R.string.app_topup_coin_150)),
            CoinOption(300, getString(R.string.app_topup_coin_300)),
            CoinOption(400, getString(R.string.app_topup_coin_400)),
            CoinOption(600, getString(R.string.app_topup_coin_600)),
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_coin_options)
        val spanCount = 3
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.addItemDecoration(CoinGridSpacingDecoration(spanCount, dpToPx(10)))
        recyclerView.adapter = CoinOptionAdapter(coinOptions, selectedPosition) { position ->
            selectedPosition = position
        }
    }

    private fun setupPayButton(view: View) {
        val btnPay = view.findViewById<TextView>(R.id.btn_pay)
        btnPay.setOnClickListener {
            VideoChatToast.show(getString(R.string.app_topup_not_ready))
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private class CoinGridSpacingDecoration(
        private val spanCount: Int,
        private val spacing: Int,
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }

    companion object {
        fun newInstance(): TopUpBottomSheetFragment {
            return TopUpBottomSheetFragment()
        }
    }
}

