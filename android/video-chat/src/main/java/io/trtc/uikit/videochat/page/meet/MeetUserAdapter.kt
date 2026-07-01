package io.trtc.uikit.videochat.page.meet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import android.content.Intent
import android.os.Bundle
import com.tencent.imsdk.v2.V2TIMConversation
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.widget.TagView
import io.trtc.uikit.videochat.common.widget.avatar.AvatarView
import io.trtc.uikit.videochat.manager.UserInfoStore
import io.trtc.uikit.videochat.page.profile.UserProfilePage

/**
 * RecyclerView Adapter for MeetPage, supporting two layouts: two-column card / single-column list.
 */
internal class MeetUserAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CARD = 0
        private const val TYPE_LIST = 1
        const val PAYLOAD_FOLLOW_STATE = "payload_follow_state"
    }

    private var items: List<V2TIMUserFullInfo> = emptyList()
    internal var isCardLayout = true

    internal fun submitList(newList: List<V2TIMUserFullInfo>) {
        val oldList = items
        items = newList.toList()
        val diff = DiffUtil.calculateDiff(UserDiffCallback(oldList, items))
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int) = if (isCardLayout) TYPE_CARD else TYPE_LIST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_CARD) {
            CardViewHolder(inflater.inflate(R.layout.videochat_item_user_card, parent, false))
        } else {
            ListViewHolder(inflater.inflate(R.layout.videochat_item_user_list, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = items[position]
        when (holder) {
            is CardViewHolder -> holder.bind(user)
            is ListViewHolder -> holder.bind(user)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_FOLLOW_STATE)) {
            val user = items[position]
            if (holder is CardViewHolder) holder.bindFollowState(user)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = items.size

    private class UserDiffCallback(
        private val oldList: List<V2TIMUserFullInfo>,
        private val newList: List<V2TIMUserFullInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos].userID == newList[newPos].userID
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val old = oldList[oldPos]
            val new = newList[newPos]
            return old.nickName == new.nickName
                && old.faceUrl == new.faceUrl
                && old.selfSignature == new.selfSignature
                && old.gender == new.gender
                && old.birthday == new.birthday
        }
    }

    internal class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivCover: ImageView = view.findViewById(R.id.iv_cover)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvOnline: TextView = view.findViewById(R.id.tv_online)
        private val layoutTags: ViewGroup = view.findViewById(R.id.layout_tags)
        private val btnFollow: TextView = view.findViewById(R.id.btn_follow)
        private val btnChat: TextView = view.findViewById(R.id.btn_chat)
        private val safeContext = view.context

        fun bind(user: V2TIMUserFullInfo) {
            loadAvatar(user)
            tvName.text = user.nickName
            tvOnline.visibility = View.VISIBLE
            bindTags(user)
            bindFollowState(user)
            btnChat.setOnClickListener {
                TUICore.startActivity("TUIC2CChatActivity", Bundle().apply {
                    putInt(TUIConstants.TUIChat.CHAT_TYPE, V2TIMConversation.V2TIM_C2C)
                    putString(TUIConstants.TUIChat.CHAT_ID, user.userID ?: "")
                })
            }
            itemView.setOnClickListener {
                safeContext.startActivity(Intent(safeContext, UserProfilePage::class.java).apply {
                    putExtra(UserProfilePage.EXTRA_USER_ID, user.userID ?: "")
                })
            }
        }

        fun bindFollowState(user: V2TIMUserFullInfo) {
            val userId = user.userID ?: ""
            val followed = UserInfoStore.shared.isFollowing(userId)
            btnFollow.text = safeContext.getString(
                if (followed) R.string.videochat_followed else R.string.videochat_follow
            )
            btnFollow.setBackgroundResource(
                if (followed) R.drawable.videochat_bg_btn_followed
                else R.drawable.videochat_bg_btn_follow
            )
            btnFollow.setOnClickListener { toggleFollow(userId) }
        }

        private fun toggleFollow(userId: String) {
            val ids = listOf(userId)
            if (UserInfoStore.shared.isFollowing(userId)) {
                UserInfoStore.shared.unFellowUsers(ids)
            } else {
                UserInfoStore.shared.fellowUser(ids)
            }
        }

        private fun loadAvatar(user: V2TIMUserFullInfo) {
            Glide.with(safeContext)
                .load(user.faceUrl)
                .placeholder(R.drawable.videochat_ic_default_avatar)
                .centerCrop()
                .into(ivCover)
        }

        private fun bindTags(user: V2TIMUserFullInfo) {
            layoutTags.removeAllViews()
            val labels = UserInfoStore.shared.getUserLabelList(user)
            labels.forEach { label ->
                val tag = TagView(safeContext, label, TagView.Style.FROSTED)
                layoutTags.addView(tag, tag.defaultLayoutParams())
            }
        }
    }

    internal class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: AvatarView = view.findViewById(R.id.avatar)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val layoutTags: ViewGroup = view.findViewById(R.id.layout_tags)
        private val tvBio: TextView = view.findViewById(R.id.tv_bio)
        private val btnChat: TextView = view.findViewById(R.id.btn_chat)
        private val ctx = view.context

        fun bind(user: V2TIMUserFullInfo) {
            bindAvatar(user)
            tvName.text = user.nickName
            tvBio.text = user.selfSignature
            bindTags(user)
            bindActions(user)
        }

        private fun bindAvatar(user: V2TIMUserFullInfo) {
            avatar.loadAvatar(user.faceUrl)
            avatar.showBorder = true
            avatar.showBadge = true
        }

        private fun bindTags(user: V2TIMUserFullInfo) {
            layoutTags.removeAllViews()
            val labels = UserInfoStore.shared.getUserLabelList(user)
            labels.forEach { label ->
                val tag = TagView(ctx, label, TagView.Style.SMALL_OUTLINED)
                layoutTags.addView(tag, tag.defaultLayoutParams())
            }
        }

        private fun bindActions(user: V2TIMUserFullInfo) {
            val userId = user.userID ?: ""
            btnChat.setOnClickListener {
                TUICore.startActivity("TUIC2CChatActivity", Bundle().apply {
                    putInt(TUIConstants.TUIChat.CHAT_TYPE, V2TIMConversation.V2TIM_C2C)
                    putString(TUIConstants.TUIChat.CHAT_ID, userId)
                })
            }
            itemView.setOnClickListener {
                ctx.startActivity(Intent(ctx, UserProfilePage::class.java).apply {
                    putExtra(UserProfilePage.EXTRA_USER_ID, userId)
                })
            }
        }
    }
}
