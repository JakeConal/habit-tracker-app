package com.example.habittracker.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.User

class LeaderboardAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val users = mutableListOf<User>()
    private val top3Users = mutableListOf<User>()

    companion object {
        private const val TYPE_PODIUM = 0
        private const val TYPE_ENTRY = 1
    }

    fun submitList(fullList: List<User>) {
        top3Users.clear()
        if (fullList.isNotEmpty()) {
            // Map the first 3 to include correct rank
            top3Users.addAll(fullList.take(3).mapIndexed { index, user ->
                user.copy(rank = index + 1)
            })
        }

        users.clear()
        if (fullList.size > 3) {
            // Map the rest to include correct rank starting from 4
            users.addAll(fullList.subList(3, fullList.size).mapIndexed { index, user ->
                user.copy(rank = index + 4)
            })
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_PODIUM else TYPE_ENTRY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PODIUM -> {
                val view = inflater.inflate(R.layout.layout_leaderboard_podium, parent, false)
                PodiumViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_leaderboard_entry, parent, false)
                EntryViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PodiumViewHolder -> holder.bind(top3Users)
            is EntryViewHolder -> holder.bind(users[position - 1])
        }
    }

    override fun getItemCount(): Int {
        return if (top3Users.isEmpty()) 0 else users.size + 1
    }

    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvPoints: TextView = itemView.findViewById(R.id.tv_points)
        private val tvScore: TextView = itemView.findViewById(R.id.tv_score)

        fun bind(user: User) {
            tvRank.text = user.rank.toString()
            tvName.text = user.name
            val pointsStr = itemView.context.getString(R.string.points_format, user.points)
            tvPoints.text = pointsStr
            tvScore.text = pointsStr

            // Load avatar
            if (user.avatarUrl != null) {
                Glide.with(itemView.context)
                    .load(user.avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person)
            }
        }
    }

    class PodiumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firstAvatar: ImageView = itemView.findViewById(R.id.iv_first_avatar)
        private val firstName: TextView = itemView.findViewById(R.id.tv_first_name)
        private val firstPoints: TextView = itemView.findViewById(R.id.tv_first_points)

        private val secondAvatar: ImageView = itemView.findViewById(R.id.iv_second_avatar)
        private val secondName: TextView = itemView.findViewById(R.id.tv_second_name)
        private val secondPoints: TextView = itemView.findViewById(R.id.tv_second_points)

        private val thirdAvatar: ImageView = itemView.findViewById(R.id.iv_third_avatar)
        private val thirdName: TextView = itemView.findViewById(R.id.tv_third_name)
        private val thirdPoints: TextView = itemView.findViewById(R.id.tv_third_points)

        fun bind(topUsers: List<User>) {
            val context = itemView.context
            if (topUsers.size >= 1) {
                firstName.text = topUsers[0].name
                firstPoints.text = context.getString(R.string.points_format, topUsers[0].points)
                loadAvatar(firstAvatar, topUsers[0].avatarUrl)
            }
            if (topUsers.size >= 2) {
                secondName.text = topUsers[1].name
                secondPoints.text = context.getString(R.string.points_format, topUsers[1].points)
                loadAvatar(secondAvatar, topUsers[1].avatarUrl)
            }
            if (topUsers.size >= 3) {
                thirdName.text = topUsers[2].name
                thirdPoints.text = context.getString(R.string.points_format, topUsers[2].points)
                loadAvatar(thirdAvatar, topUsers[2].avatarUrl)
            }
        }

        private fun loadAvatar(imageView: ImageView, url: String?) {
            if (!url.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_person)
            }
        }
    }
}