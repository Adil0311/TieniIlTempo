package com.uniupo.tieniiltempo.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uniupo.TieniITempo.R
import com.uniupo.tieniiltempo.data.model.SubActivity

class UserSubActivityAdapter(
    private val onCompleteClick: (SubActivity) -> Unit
) : ListAdapter<SubActivity, UserSubActivityAdapter.SubActivityViewHolder>(SubActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_sub_activity, parent, false)
        return SubActivityViewHolder(view, onCompleteClick)
    }

    override fun onBindViewHolder(holder: SubActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubActivityViewHolder(
        itemView: View,
        private val onCompleteClick: (SubActivity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvSubActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvSubActivityDescription)
        private val tvMaxTime: TextView = itemView.findViewById(R.id.tvMaxTime)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val ivLocationRequired: ImageView = itemView.findViewById(R.id.ivLocationRequired)
        private val ivCompleted: ImageView = itemView.findViewById(R.id.ivCompleted)
        private val btnComplete: Button = itemView.findViewById(R.id.btnComplete)

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

            if (subActivity.status == "completed") {
                ivCompleted.visibility = View.VISIBLE
                btnComplete.visibility = View.GONE

                // Mostra l'immagine caricata dall'utente se presente
                if (subActivity.userImage != null) {
                    ivImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(subActivity.userImage)
                        .into(ivImage)
                } else {
                    ivImage.visibility = View.GONE
                }
            } else {
                ivCompleted.visibility = View.GONE
                btnComplete.visibility = View.VISIBLE
                ivImage.visibility = View.GONE
            }

            btnComplete.setOnClickListener {
                onCompleteClick(subActivity)
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