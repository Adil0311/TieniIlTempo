package com.uniupo.tieniiltempo.ui.chat

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
import com.uniupo.tieniiltempo.data.model.Message
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            tvMessage.text = message.text
            tvTime.text = dateFormat.format(message.timestamp)

            if (message.imageUrl != null) {
                ivImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            tvMessage.text = message.text
            tvTime.text = dateFormat.format(message.timestamp)

            if (message.imageUrl != null) {
                ivImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}