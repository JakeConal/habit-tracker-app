package com.example.habittracker.ui.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.databinding.ItemChallengeReviewBinding

class ChallengeReviewAdapter(
    private val onItemClick: (Challenge) -> Unit
) : ListAdapter<Challenge, ChallengeReviewAdapter.ViewHolder>(ChallengeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemChallengeReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challenge: Challenge) {
            binding.root.setOnClickListener { onItemClick(challenge) }
            binding.tvChallengeTitle.text = challenge.title
            binding.tvChallengeDescription.text = challenge.description
            binding.tvBadge.text = challenge.duration.duration

            // Set badge background color based on duration
            val context = binding.root.context
            val badgeDrawable = when (challenge.duration.name) {
                "SEVEN_DAYS" -> R.drawable.badge_color_cyan
                "THIRTY_DAYS" -> R.drawable.badge_color_green
                "HUNDRED_DAYS" -> R.drawable.badge_color_yellow
                else -> R.drawable.badge_color_cyan
            }
            binding.tvBadge.setBackgroundResource(badgeDrawable)

            if (challenge.imgURL.isNotEmpty()) {
                Glide.with(context)
                    .load(challenge.imgURL)
                    .centerCrop()
                    .placeholder(R.color.secondary_gray)
                    .into(binding.ivChallengeImage)
            } else {
                binding.ivChallengeImage.setImageResource(R.color.secondary_gray)
            }
        }
    }

    class ChallengeDiffCallback : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem == newItem
        }
    }
}
