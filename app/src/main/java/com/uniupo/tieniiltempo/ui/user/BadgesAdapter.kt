// app/src/main/java/com/uniupo/tieniiltempo/ui/user/BadgesAdapter.kt
package com.uniupo.tieniiltempo.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uniupo.TieniITempo.R

class BadgesAdapter : ListAdapter<String, BadgesAdapter.BadgeViewHolder>(BadgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBadge: ImageView = itemView.findViewById(R.id.ivBadge)
        private val tvBadgeTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)

        fun bind(badgeName: String) {
            // Imposta l'immagine del badge in base al nome
            val badgeDrawable = when (badgeName) {
                "primo_completamento" -> R.drawable.ic_badge_first
                "cinque_completamenti" -> R.drawable.ic_badge_five
                "puntuale" -> R.drawable.ic_badge_ontime
                else -> R.drawable.ic_badge_generic
            }

            ivBadge.setImageResource(badgeDrawable)

            // Imposta il titolo del badge in italiano
            val badgeTitle = when (badgeName) {
                "primo_completamento" -> "Prima attività"
                "cinque_completamenti" -> "Cinque attività"
                "puntuale" -> "Sempre puntuale"
                else -> badgeName
            }

            tvBadgeTitle.text = badgeTitle
        }
    }

    class BadgeDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}