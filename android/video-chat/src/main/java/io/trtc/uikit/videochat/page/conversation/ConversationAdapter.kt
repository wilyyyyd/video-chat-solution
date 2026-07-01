package io.trtc.uikit.videochat.page.conversation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tencent.qcloud.tuikit.tuiconversation.classicui.widget.ConversationListAdapter
import io.trtc.uikit.videochat.R

/**
 * Extends ConversationListAdapter, overrides normal conversation ViewHolder with a custom card layout.
 * Special types (search header, loading, empty state) are delegated back to the parent class.
 */
internal class ConversationAdapter : ConversationListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val isNormalConversation = viewType != ITEM_TYPE_HEADER_SEARCH
                && viewType != ITEM_TYPE_FOOTER_LOADING
                && viewType != ITEM_TYPE_NULL_DATA

        if (isNormalConversation) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.videochat_conversation_item, parent, false)
            val holder = ConversationHolder(view)
            holder.setAdapter(this)
            return holder
        }
        return super.onCreateViewHolder(parent, viewType)
    }
}
