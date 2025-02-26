package com.uniupo.tieniiltempo.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.data.model.ChatPreview
import java.text.SimpleDateFormat
import java.util.Locale

class ChatsAdapter(
    private val onChatClick: (ChatPreview) -> Unit
) : ListAdapter<ChatPreview, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view, onChatClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(
        itemView: View,
        private val onChatClick: (ChatPreview) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvActivityTitle: TextView = itemView.findViewById(R.id.tvActivityTitle)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        private val ivUnread: ImageView = itemView.findViewById(R.id.ivUnread)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(chatPreview: ChatPreview) {
            tvActivityTitle.text = chatPreview.activityTitle
            tvUsername.text = chatPreview.userName
            tvLastMessage.text = chatPreview.lastMessage
            tvTimestamp.text = if (chatPreview.lastMessageTime != null) {
                dateFormat.format(chatPreview.lastMessageTime)
            } else {
                ""
            }

            if (chatPreview.userImage != null) {
                Glide.with(itemView.context)
                    .load(chatPreview.userImage)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivUserImage)
            } else {
                ivUserImage.setImageResource(R.drawable.ic_person)
            }

            ivUnread.visibility = if (chatPreview.hasUnreadMessages) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onChatClick(chatPreview)
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatPreview>() {
        override fun areItemsTheSame(oldItem: ChatPreview, newItem: ChatPreview): Boolean {
            return oldItem.activityId == newItem.activityId
        }

        override fun areContentsTheSame(oldItem: ChatPreview, newItem: ChatPreview): Boolean {
            return oldItem == newItem
        }
    }
}