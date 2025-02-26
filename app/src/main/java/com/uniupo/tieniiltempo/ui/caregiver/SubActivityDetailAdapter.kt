package com.uniupo.tieniiltempo.ui.caregiver

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
import com.uniupo.tieniiltempo.data.model.SubActivity

class SubActivityDetailAdapter : ListAdapter<SubActivity, SubActivityDetailAdapter.SubActivityViewHolder>(SubActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_activity_detail, parent, false)
        return SubActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvSubActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvSubActivityDescription)
        private val tvMaxTime: TextView = itemView.findViewById(R.id.tvMaxTime)
        private val tvActualTime: TextView = itemView.findViewById(R.id.tvActualTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvUserComment: TextView = itemView.findViewById(R.id.tvUserComment)
        private val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        private val ivLocationRequired: ImageView = itemView.findViewById(R.id.ivLocationRequired)
        private val ivParallel: ImageView = itemView.findViewById(R.id.ivParallel)

        fun bind(subActivity: SubActivity) {
            tvTitle.text = subActivity.title
            tvDescription.text = subActivity.description

            tvMaxTime.text = if (subActivity.maxTime != null) {
                "Tempo max: ${subActivity.maxTime} sec"
            } else {
                "Nessun tempo massimo"
            }

            tvActualTime.text = if (subActivity.actualTime != null) {
                "Tempo impiegato: ${subActivity.actualTime} sec"
            } else {
                "Tempo non registrato"
            }
            tvActualTime.visibility = if (subActivity.status == "completed") View.VISIBLE else View.GONE

            val statusText = when(subActivity.status) {
                "pending" -> "In attesa"
                "in_progress" -> "In corso"
                "completed" -> "Completata"
                else -> subActivity.status
            }
            tvStatus.text = statusText

            tvUserComment.text = subActivity.userComment ?: "Nessun commento"
            tvUserComment.visibility = if (subActivity.userComment != null) View.VISIBLE else View.GONE

            if (subActivity.userImage != null) {
                ivUserImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(subActivity.userImage)
                    .into(ivUserImage)
            } else {
                ivUserImage.visibility = View.GONE
            }

            ivLocationRequired.visibility = if (subActivity.requireLocation) View.VISIBLE else View.GONE
            ivParallel.visibility = if (subActivity.isParallel) View.VISIBLE else View.GONE
        }
    }

    class SubActivityDiffCallback : DiffUtil.ItemCallback<SubActivity>() {
        override fun areItemsTheSame(oldItem: SubActivity, newItem: SubActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubActivity, newItem: SubActivity): Boolean {
            return oldItem == newItem
        }
    }
}