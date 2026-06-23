package io.trtc.uikit.videochat.page.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationGroupBean
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo
import com.tencent.qcloud.tuikit.tuiconversation.classicui.interfaces.OnConversationAdapterListener
import com.tencent.qcloud.tuikit.tuiconversation.classicui.page.TUIFoldedConversationActivity
import com.tencent.qcloud.tuikit.tuiconversation.classicui.util.TUIConversationUtils
import com.tencent.qcloud.tuikit.tuiconversation.classicui.widget.ConversationListLayout
import com.tencent.qcloud.tuikit.tuiconversation.config.classicui.TUIConversationConfigClassic
import com.tencent.qcloud.tuikit.tuiconversation.interfaces.IConversationListAdapter
import com.tencent.qcloud.tuikit.tuiconversation.presenter.ConversationPresenter
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.common.utils.VideoChatDataReporter

/**
 * 会话列表页面，纯会话列表展示。
 *
 * 使用方式：
 * ```
 * val conversationPage = ConversationPage()
 * supportFragmentManager.beginTransaction()
 *     .add(R.id.container, conversationPage)
 *     .commit()
 * ```
 */
class ConversationPage : Fragment() {

    private lateinit var listLayout: ConversationListLayout
    private lateinit var presenter: ConversationPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = createRootView()
        listLayout = createListLayout()
        root.addView(listLayout)
        VideoChatDataReporter.reportMetrics(VideoChatDataReporter.CONVERSATION_PAGE)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPresenter()
        setupAdapter()
        setupClickListener()
    }

    override fun onResume() {
        super.onResume()
        if (::presenter.isInitialized) presenter.setFocus(true)
    }

    override fun onPause() {
        super.onPause()
        if (::presenter.isInitialized) presenter.setFocus(false)
    }

    private fun createRootView(): FrameLayout {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = Theme.pageBackground()
        }
    }

    private fun createListLayout(): ConversationListLayout {
        return ConversationListLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x00000000)
            setPadding(0, dip2px(4f), 0, dip2px(4f))
            clipToPadding = false
        }
    }

    private fun setupPresenter() {
        presenter = ConversationPresenter().apply {
            setConversationListener()
            setShowType(ConversationPresenter.SHOW_TYPE_CONVERSATION_LIST_WITH_FOLD)
            setConversationGroupType(ConversationGroupBean.CONVERSATION_GROUP_TYPE_DEFAULT)
        }
        listLayout.setPresenter(presenter)
        TUIConversationConfigClassic.setShowUserOnlineStatusIcon(true)
    }

    private fun setupAdapter() {
        val adapter = ConversationAdapter().apply { setShowFoldedStyle(true) }
        listLayout.setAdapter(adapter as IConversationListAdapter)
        presenter.setAdapter(adapter)
        listLayout.loadConversation()
        listLayout.loadMarkedConversation()
    }

    private fun setupClickListener() {
        listLayout.setOnConversationAdapterListener(conversationListener())
    }

    private fun conversationListener(): OnConversationAdapterListener {
        return object : OnConversationAdapterListener {
            override fun onItemClick(view: View, viewType: Int, info: ConversationInfo) {
                if (info.isMarkFold) {
                    startActivity(
                        android.content.Intent(activity, TUIFoldedConversationActivity::class.java)
                    )
                } else {
                    TUIConversationUtils.startChatActivity(info)
                }
            }
            override fun onItemLongClick(view: View, info: ConversationInfo) {}
            override fun onConversationChanged(data: MutableList<ConversationInfo>?) {}
        }
    }
}
