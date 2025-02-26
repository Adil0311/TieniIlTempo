package com.uniupo.tieniiltempo.ui.caregiver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.data.model.Activity
import java.text.SimpleDateFormat
import java.util.Locale

class ActivitiesAdapter(
    private val onActivityClick: (Activity) -> Unit
) : ListAdapter<Activity, ActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view, onActivityClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(
        itemView: View,
        private val onActivityClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvActivityDescription)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvAssignedTo: TextView = itemView.findViewById(R.id.tvAssignedTo)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(activity: Activity) {
            tvTitle.text = activity.title
            tvDescription.text = activity.description
            tvDate.text = dateFormat.format(activity.createdAt)
            tvAssignedTo.text = "Utente: ${activity.userId}"

            val statusText = when(activity.status) {
                "pending" -> "In attesa"
                "in_progress" -> "In corso"
                "completed" -> "Completata"
                else -> activity.status
            }
            tvStatus.text = statusText

            val statusColor = when(activity.status) {
                "pending" -> R.color.colorPending
                "in_progress" -> R.color.colorInProgress
                "completed" -> R.color.colorCompleted
                else -> R.color.colorPending
            }
            tvStatus.setTextColor(itemView.context.getColor(statusColor))

            cardView.setOnClickListener {
                onActivityClick(activity)
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}