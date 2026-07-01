package io.trtc.uikit.demo.meet

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.page.meet.MeetPage
import io.trtc.uikit.videochat.R as VideoChatR

/**
 * Core multi-tab UI container that manages ViewPager2 + top tabs + layout toggle.
 */
class MeetPageFragment : Fragment() {

    private data class PageConfig(var pageTitle: String, val pageTag: String)

    private val pageConfigs = mutableListOf<PageConfig>()
    private val pageViews = mutableMapOf<String, MeetPage>()
    private var listener: MeetPageListener? = null
    private var isCardLayout = false

    private var viewPager: ViewPager2? = null
    private var layoutTabs: LinearLayout? = null
    private var btnToggleLayout: ImageView? = null
    private val tabViews = mutableListOf<View>()

    fun setMeetPageListener(listener: MeetPageListener) { this.listener = listener }

    fun addPage(pageTag: String, pageTitle: String) {
        pageConfigs.add(PageConfig(pageTitle, pageTag))
    }

    fun refreshUsers(pageTag: String, userList: List<V2TIMUserFullInfo>) {
        pageViews[pageTag]?.setPageUsers(userList, hasMore = true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(VideoChatR.layout.videochat_fragment_meet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(VideoChatR.id.view_pager)
        layoutTabs = view.findViewById(VideoChatR.id.layout_tabs)
        btnToggleLayout = view.findViewById(VideoChatR.id.btn_toggle_layout)

        initPages()
        rebuildViewPager()
        rebuildTabs()

        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabStyle(position)
                if (position < pageConfigs.size) {
                    listener?.onPageSelected(pageConfigs[position].pageTag)
                }
            }
        })

        btnToggleLayout?.apply {
            setColorFilter(Theme.PINK)
            setOnClickListener {
                isCardLayout = !isCardLayout
                val template = if (isCardLayout) MeetPage.Template.GRID else MeetPage.Template.LIST
                setImageResource(
                    if (isCardLayout) VideoChatR.drawable.videochat_ic_layout_grid
                    else VideoChatR.drawable.videochat_ic_layout_list
                )
                setColorFilter(Theme.PINK)
                pageViews.values.forEach { it.setLayoutTemplate(template) }
            }
        }
    }

    private fun initPages() {
        pageViews.clear()
        pageConfigs.forEach { config ->
            val pageView = MeetPage(requireContext())
            pageView.setListener(object : MeetPage.Listener {
                override fun onRefreshRequested() { listener?.onRefreshRequested(config.pageTag) }
                override fun onLoadMoreRequested() { listener?.onLoadMoreRequested(config.pageTag) }
            })
            pageViews[config.pageTag] = pageView
        }
    }

    private fun rebuildViewPager() {
        val orderedPages = pageConfigs.map { pageViews[it.pageTag]!! }
        viewPager?.adapter = object : RecyclerView.Adapter<PageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
                val container = FrameLayout(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                return PageViewHolder(container)
            }

            override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
                val container = holder.itemView as FrameLayout
                container.removeAllViews()
                val pageView = orderedPages[position]
                (pageView.parent as? ViewGroup)?.removeView(pageView)
                container.addView(pageView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }

            override fun getItemCount() = orderedPages.size
        }
    }

    private class PageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun rebuildTabs() {
        layoutTabs?.removeAllViews()
        tabViews.clear()

        pageConfigs.forEachIndexed { index, config ->
            val tabView = LayoutInflater.from(context)
                .inflate(VideoChatR.layout.videochat_item_tab, layoutTabs, false)
            val tvTitle = tabView.findViewById<TextView>(VideoChatR.id.tv_tab_title)
            tvTitle.text = config.pageTitle

            tabView.setOnClickListener {
                listener?.onPageTitleClicked(config.pageTag)
                viewPager?.currentItem = index
            }

            layoutTabs?.addView(tabView)
            tabViews.add(tabView)
        }

        if (tabViews.isNotEmpty()) {
            updateTabStyle(viewPager?.currentItem ?: 0)
        }
    }

    private fun updateTabStyle(selectedIndex: Int) {
        tabViews.forEachIndexed { index, tabView ->
            val selected = index == selectedIndex
            val tvTitle = tabView.findViewById<TextView>(VideoChatR.id.tv_tab_title)
            val indicator = tabView.findViewById<View>(VideoChatR.id.view_indicator)

            tvTitle.textSize = if (selected) 16f else 14f
            tvTitle.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            Theme.toggleTabText(tvTitle, selected)
            indicator.visibility = if (selected) View.VISIBLE else View.GONE
        }
    }
}
