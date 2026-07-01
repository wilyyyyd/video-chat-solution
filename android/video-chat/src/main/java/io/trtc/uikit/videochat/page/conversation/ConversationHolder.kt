package io.trtc.uikit.videochat.page.conversation

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import com.tencent.qcloud.tuicore.util.ScreenUtil.dip2px
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo
import com.tencent.qcloud.tuikit.tuiconversation.classicui.widget.ConversationCommonHolder
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme

/**
 * Instagram-style conversation list ViewHolder: rounded card + circular avatar + pink unread badge.
 */
internal class ConversationHolder(itemView: View) : ConversationCommonHolder(itemView) {

    init {
        rootView.elevation = dip2px(2f).toFloat()
        rootView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dip2px(12f).toFloat())
            }
        }
        rootView.clipToOutline = true

        conversationIconView?.setRadius(100)
    }

    override fun layoutViews(conversation: ConversationInfo?, position: Int) {
        super.layoutViews(conversation, position)

        rootView.findViewById<View>(com.tencent.qcloud.tuikit.tuiconversation.R.id.view_line)
            ?.visibility = View.GONE

        leftItemLayout?.setBackgroundResource(R.drawable.videochat_bg_conversation_item)

        unreadText?.setPaintColor(Theme.PINK)
    }
}
