package com.example.habittracker.ui.challenge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.BadgeColor
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeWithStatus
import com.google.android.material.imageview.ShapeableImageView

class ChallengeAdapter (
    private val challengeList: Array<Challenge>,
    private val challengeStatusList: List<ChallengeWithStatus> = emptyList(),
    private val onChallengeClick: ((Challenge) -> Unit)? = null,
    private val onCreateChallengeClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CHALLENGE = 0
        private const val VIEW_TYPE_CREATE = 1
    }

    // Create a map for faster lookup by challenge ID
    private val statusMap = challengeStatusList.associateBy { it.challenge.id }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val challengeImg = itemView.findViewById<ShapeableImageView>(R.id.ivChallengeImage)
        val durationBadge = itemView.findViewById<TextView>(R.id.tvBadge)
        val challengeTitle = itemView.findViewById<TextView>(R.id.tvChallengeTitle)
        val challengeDesc = itemView.findViewById<TextView>(R.id.tvChallengeDescription)
        val joinedIndicator = itemView.findViewById<TextView?>(R.id.tvJoinedIndicator) // Optional view to show join status
    }

    class CreateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define views for the create challenge item if needed
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHALLENGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_challenge_card, parent, false)
                ViewHolder(view)
            }
            VIEW_TYPE_CREATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_create_challenge, parent, false)
                CreateViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is ViewHolder -> bindChallengeViewHolder(holder, position)
            is CreateViewHolder -> bindCreateViewHolder(holder)
        }
    }

    private fun bindChallengeViewHolder(holder: ViewHolder, position: Int) {
        val challenge = challengeList[position] // No offset needed since create item is at the end
        // Look up join status by challenge ID instead of position
        val isJoined = statusMap[challenge.id]?.isJoined ?: false

        Glide.with(holder.itemView.context)
            .load(challenge.imgURL)
            .placeholder(R.color.secondary_gray)
            .centerCrop()
            .into(holder.challengeImg)

        holder.durationBadge.apply {
            text = challenge.duration.duration
            val bgRes = when (challenge.duration.color) {
                BadgeColor.CYAN -> R.drawable.badge_color_cyan
                BadgeColor.GREEN -> R.drawable.badge_color_green
                BadgeColor.YELLOW -> R.drawable.badge_color_yellow
            }
            setBackgroundResource(bgRes)
        }

        holder.challengeTitle.text = challenge.title
        holder.challengeDesc.text = challenge.description

        // Show join status if indicator view exists
        holder.joinedIndicator?.apply {
            if (isJoined) {
                text = "Joined"
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onChallengeClick?.invoke(challenge)
        }
    }

    private fun bindCreateViewHolder(holder: CreateViewHolder) {
        // Handle binding for the create challenge view holder
        holder.itemView.setOnClickListener {
            onCreateChallengeClick?.invoke()
        }
    }

    override fun getItemCount(): Int = challengeList.size + 1 // +1 for create challenge item

    override fun getItemViewType(position: Int): Int {
        return if (position == challengeList.size) {
            VIEW_TYPE_CREATE
        } else {
            VIEW_TYPE_CHALLENGE
        }
    }
}
