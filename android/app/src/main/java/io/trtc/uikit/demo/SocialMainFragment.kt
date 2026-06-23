package io.trtc.uikit.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.demo.message.MessagePage
import io.trtc.uikit.videochat.page.settings.SettingsPage
import io.trtc.uikit.demo.manager.SocialManager
import io.trtc.uikit.demo.meet.MeetPageFragment

/**
 * 1v1 社交娱乐主容器 Fragment（底部导航栏：交友、消息、我的）
 */
class SocialMainFragment : Fragment() {

    val meetPageFragment = MeetPageFragment()
    private val messageFragment = MessagePage()
    private val mineFragment = SettingsPage()
    private var activeFragment: Fragment = meetPageFragment

    private var currentTab = 0

    private lateinit var navTabs: Array<View>
    private lateinit var navBgs: Array<View>
    private lateinit var navIcons: Array<ImageView>
    private lateinit var navLabels: Array<TextView>
    private lateinit var navMessageBadge: TextView

    private val iconNormal = intArrayOf(
        R.drawable.app_btn_meet_unselected,
        R.drawable.app_btn_message_unselected,
        R.drawable.app_ic_tab_mine
    )
    private val iconSelected = intArrayOf(
        R.drawable.app_btn_meet_selected,
        R.drawable.app_btn_message_selected,
        R.drawable.app_btn_mine_selected
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.videochat_fragment_social_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navTabs = arrayOf(
            view.findViewById(R.id.nav_tab_meet),
            view.findViewById(R.id.nav_tab_message),
            view.findViewById(R.id.nav_tab_mine)
        )
        navBgs = arrayOf(
            view.findViewById(R.id.nav_meet_bg),
            view.findViewById(R.id.nav_message_bg),
            view.findViewById(R.id.nav_mine_bg)
        )
        navIcons = arrayOf(
            view.findViewById(R.id.nav_meet_icon),
            view.findViewById(R.id.nav_message_icon),
            view.findViewById(R.id.nav_mine_icon)
        )
        navLabels = arrayOf(
            view.findViewById(R.id.nav_meet_label),
            view.findViewById(R.id.nav_message_label),
            view.findViewById(R.id.nav_mine_label)
        )
        navMessageBadge = view.findViewById(R.id.nav_message_badge)

        childFragmentManager.beginTransaction()
            .add(R.id.container_page, mineFragment, "mine").hide(mineFragment)
            .add(R.id.container_page, messageFragment, "message").hide(messageFragment)
            .add(R.id.container_page, meetPageFragment, "meet")
            .commit()

        navTabs.forEachIndexed { index, tab ->
            tab.setOnClickListener { selectTab(index) }
        }

        selectTab(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        socialManager?.stopObservingUnreadCount()
    }

    private var socialManager: SocialManager? = null

    fun observeUnreadCount(manager: SocialManager?) {
        socialManager = manager
        manager?.startObservingUnreadCount(object : SocialManager.UnreadCountObserver {
            override fun onCountChanged(totalUnread: Long) {
                val text = if (totalUnread > 99) "99+" else totalUnread.toString()
                navMessageBadge.text = text
                navMessageBadge.visibility = if (totalUnread > 0) View.VISIBLE else View.GONE
            }
        })
    }

    private fun selectTab(index: Int) {
        if (index == currentTab && activeFragment.isAdded) return
        currentTab = index

        for (i in navTabs.indices) {
            val selected = i == index
            navBgs[i].visibility = if (selected) View.VISIBLE else View.GONE
            navIcons[i].setImageResource(if (selected) iconSelected[i] else iconNormal[i])
            val scale = if (selected) 1.1f else 1.0f
            navTabs[i].scaleX = scale
            navTabs[i].scaleY = scale
            navLabels[i].setTextColor(if (selected) Theme.PINK else Theme.TEXT_HINT)
            navLabels[i].setTypeface(null, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }

        val target = when (index) {
            0 -> meetPageFragment
            1 -> messageFragment
            2 -> mineFragment
            else -> meetPageFragment
        }
        if (target != activeFragment) {
            childFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }
}
