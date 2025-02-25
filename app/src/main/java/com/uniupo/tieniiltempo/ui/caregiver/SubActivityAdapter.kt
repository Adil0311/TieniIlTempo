package com.uniupo.tieniiltempo.ui.caregiver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.data.model.SubActivity

class SubActivityAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<SubActivity, SubActivityAdapter.SubActivityViewHolder>(SubActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_activity, parent, false)
        return SubActivityViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: SubActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubActivityViewHolder(
        itemView: View,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvSubActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvSubActivityDescription)
        private val tvMaxTime: TextView = itemView.findViewById(R.id.tvMaxTime)
        private val ivLocationRequired: ImageView = itemView.findViewById(R.id.ivLocationRequired)
        private val ivParallel: ImageView = itemView.findViewById(R.id.ivParallel)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteSubActivity)

        fun bind(subActivity: SubActivity) {
            tvTitle.text = subActivity.title
            tvDescription.text = subActivity.description

            tvMaxTime.text = if (subActivity.maxTime != null) {
                "Tempo max: ${subActivity.maxTime} sec"
            } else {
                "Nessun tempo massimo"
            }

            ivLocationRequired.visibility =
                if (subActivity.requireLocation) View.VISIBLE else View.GONE

            ivParallel.visibility =
                if (subActivity.isParallel) View.VISIBLE else View.GONE

            btnDelete.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
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