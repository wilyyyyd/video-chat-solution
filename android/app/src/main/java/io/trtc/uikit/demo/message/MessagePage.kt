package io.trtc.uikit.demo.message

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tencent.qcloud.tuikit.tuicontact.TUIContactConstants
import com.tencent.qcloud.tuikit.tuicontact.classicui.pages.AddMoreActivity
import com.tencent.qcloud.tuikit.tuicontact.classicui.pages.TUIContactFragment
import com.tencent.qcloud.tuikit.tuisearch.classicui.page.SearchMainActivity
import io.trtc.uikit.demo.R
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.page.conversation.ConversationPage

/**
 * 消息页面，顶部 Tab 切换消息 / 联系人。
 * 由 app 层组装，联系人能力在 app 层集成。
 */
class MessagePage : Fragment() {

    private lateinit var tabMessages: TextView
    private lateinit var tabContacts: TextView
    private lateinit var indicatorMessages: View
    private lateinit var indicatorContacts: View
    private lateinit var viewPager: ViewPager2
    private lateinit var btnAction: FrameLayout
    private lateinit var iconAction: ImageView
    private lateinit var textAction: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.app_page_message, container, false)
        initViews(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupTabClick()
        setupActionClick()
        updateTabStyle(0)
        updateActionButton(0)
    }

    private fun initViews(root: View) {
        tabMessages = root.findViewById(R.id.tv_tab_messages)
        tabContacts = root.findViewById(R.id.tv_tab_contacts)
        indicatorMessages = root.findViewById(R.id.indicator_messages)
        indicatorContacts = root.findViewById(R.id.indicator_contacts)
        viewPager = root.findViewById(R.id.view_pager)
        btnAction = root.findViewById(R.id.btn_action)
        iconAction = root.findViewById(R.id.icon_action)
        textAction = root.findViewById(R.id.text_action)
    }

    private fun setupViewPager() {
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(pos: Int) = when (pos) {
                0 -> ConversationPage()
                else -> TUIContactFragment()
            }
        }
        viewPager.registerOnPageChangeCallback(pageChangeCallback())
    }

    private fun setupTabClick() {
        val tabMessagesLayout: View = requireView().findViewById(R.id.tab_messages)
        val tabContactsLayout: View = requireView().findViewById(R.id.tab_contacts)
        tabMessagesLayout.setOnClickListener { viewPager.currentItem = 0 }
        tabContactsLayout.setOnClickListener { viewPager.currentItem = 1 }
    }

    private fun setupActionClick() {
        btnAction.setOnClickListener { handleActionClick() }
    }

    private fun pageChangeCallback(): ViewPager2.OnPageChangeCallback {
        return object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabStyle(position)
                updateActionButton(position)
            }
        }
    }

    private fun updateTabStyle(selectedIndex: Int) {
        val isMessages = selectedIndex == 0
        applyTabState(tabMessages, indicatorMessages, isMessages)
        applyTabState(tabContacts, indicatorContacts, !isMessages)
    }

    private fun applyTabState(tab: TextView, indicator: View, selected: Boolean) {
        val size = if (selected) 16f else 14f
        val style = if (selected) Typeface.BOLD else Typeface.NORMAL
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        tab.setTypeface(null, style)
        Theme.toggleTabText(tab, selected)
        indicator.visibility = if (selected) View.VISIBLE else View.GONE
    }

    private fun updateActionButton(selectedIndex: Int) {
        if (selectedIndex == 0) showSearchIcon() else showAddIcon()
    }

    private fun showSearchIcon() {
        iconAction.visibility = View.VISIBLE
        textAction.visibility = View.GONE
        iconAction.setImageResource(android.R.drawable.ic_menu_search)
        iconAction.setColorFilter(Theme.PINK)
    }

    private fun showAddIcon() {
        iconAction.visibility = View.GONE
        textAction.visibility = View.VISIBLE
        Theme.applyGradientText(textAction)
    }

    private fun handleActionClick() {
        val intent = if (viewPager.currentItem == 0) {
            Intent(activity, SearchMainActivity::class.java)
        } else {
            Intent(activity, AddMoreActivity::class.java).apply {
                putExtra(TUIContactConstants.GroupType.GROUP, false)
            }
        }
        startActivity(intent)
    }
}
