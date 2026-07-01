package io.trtc.uikit.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import io.trtc.uikit.videochat.page.call.VideoCallPage
import io.trtc.uikit.demo.manager.SocialManager
import io.trtc.uikit.demo.meet.MeetPageListener
import io.trtc.uikit.demo.topup.TopUpBottomSheetFragment

class SocialMainActivity : AppCompatActivity() {

    private val socialMainFragment = SocialMainFragment()
    private var socialManager: SocialManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        socialManager = SocialManager(getString(R.string.app_bio_fallback))
        socialManager?.callTimerCallback = object : SocialManager.CallTimerCallback {
            override fun onTimeoutWarning(remainingSeconds: Int) {
                showTopUpSheet()
            }

            override fun onCallTimeout() {
            }
        }

        val meetFragment = socialMainFragment.meetPageFragment
        meetFragment.addPage(pageTag = NEARBY_PAGE_TAG, pageTitle = getString(R.string.app_page_nearby))
        meetFragment.addPage(pageTag = FOLLOW_PAGE_TAG, pageTitle = getString(R.string.app_page_follow))

        meetFragment.setMeetPageListener(object : MeetPageListener {
            override fun onPageSelected(pageTag: String) {
                loadPageData(pageTag)
            }

            override fun onPageTitleClicked(pageTag: String) {}
            override fun onRefreshRequested(pageTag: String) {
                loadPageData(pageTag)
            }

            override fun onLoadMoreRequested(pageTag: String) {}
        })

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_main, socialMainFragment)
            .commit()
        socialMainFragment.observeUnreadCount(socialManager)

        loadPageData(NEARBY_PAGE_TAG)
        loadPageData(FOLLOW_PAGE_TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        socialManager?.reset()
    }

    private fun loadPageData(pageTag: String) {
        val usersCallback = object : SocialManager.UsersCallback {
            override fun onResult(users: List<V2TIMUserFullInfo>) {
                socialMainFragment.meetPageFragment.refreshUsers(pageTag, users)
            }

            override fun onError() {
            }
        }
        when (pageTag) {
            NEARBY_PAGE_TAG -> socialManager?.fetchNearbyUsers(usersCallback)
            FOLLOW_PAGE_TAG -> socialManager?.fetchFollowingList(usersCallback)
        }
    }

    private fun showTopUpSheet() {
        val callActivity = VideoCallPage.currentInstance?.get() ?: return
        val fragmentManager = callActivity.supportFragmentManager
        if (fragmentManager.findFragmentByTag(TOP_UP_SHEET_TAG) != null) return
        TopUpBottomSheetFragment.newInstance().show(fragmentManager, TOP_UP_SHEET_TAG)
    }

    companion object {
        private const val NEARBY_PAGE_TAG = "nearby"
        private const val FOLLOW_PAGE_TAG = "follow"
        private const val TOP_UP_SHEET_TAG = "top_up_sheet"
    }
}
