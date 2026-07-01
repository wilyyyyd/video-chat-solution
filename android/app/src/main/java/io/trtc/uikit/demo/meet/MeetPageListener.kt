package io.trtc.uikit.demo.meet

/**
 * MeetPage interaction event protocol.
 */
interface MeetPageListener {
    fun onPageSelected(pageTag: String)
    fun onPageTitleClicked(pageTag: String)
    fun onRefreshRequested(pageTag: String)
    fun onLoadMoreRequested(pageTag: String)
}
