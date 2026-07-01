package io.trtc.uikit.demo.topup

/**
 * Data model for top-up coin options.
 *
 * @param amount number of coins
 * @param description descriptive text (e.g. "≈5 min call")
 */
data class CoinOption(
    val amount: Int,
    val description: String,
)
