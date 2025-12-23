package com.example.habittracker.ui.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.User

class LeaderboardFragment : Fragment() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupTopThree(view)
        loadLeaderboardData()
    }

    private fun setupRecyclerView(view: View) {
        rvLeaderboard = view.findViewById(R.id.rv_leaderboard)
        adapter = LeaderboardAdapter()
        rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.adapter = adapter
    }

    private fun setupTopThree(view: View) {
        // Setup top 3 users
        val firstAvatar = view.findViewById<ImageView>(R.id.iv_first_avatar)
        val firstName = view.findViewById<TextView>(R.id.tv_first_name)
        val firstPoints = view.findViewById<TextView>(R.id.tv_first_points)

        val secondAvatar = view.findViewById<ImageView>(R.id.iv_second_avatar)
        val secondName = view.findViewById<TextView>(R.id.tv_second_name)
        val secondPoints = view.findViewById<TextView>(R.id.tv_second_points)

        val thirdAvatar = view.findViewById<ImageView>(R.id.iv_third_avatar)
        val thirdName = view.findViewById<TextView>(R.id.tv_third_name)
        val thirdPoints = view.findViewById<TextView>(R.id.tv_third_points)

        // Load top 3 data
        loadTopThreeData(firstAvatar, firstName, firstPoints,
                        secondAvatar, secondName, secondPoints,
                        thirdAvatar, thirdName, thirdPoints)
    }

    private fun loadTopThreeData(
        firstAvatar: ImageView, firstName: TextView, firstPoints: TextView,
        secondAvatar: ImageView, secondName: TextView, secondPoints: TextView,
        thirdAvatar: ImageView, thirdName: TextView, thirdPoints: TextView
    ) {
        // Mock data - replace with actual data loading
        val topUsers = listOf(
            User("1", "Emma Wilson", null, 2400, 1),
            User("2", "Michael Chen", null, 2150, 2),
            User("3", "Sarah Johnson", null, 1980, 3)
        )

        // First place
        firstName.text = topUsers[0].name
        firstPoints.text = "${topUsers[0].points} pts"
        loadAvatar(firstAvatar, topUsers[0].avatarUrl)

        // Second place
        secondName.text = topUsers[1].name
        secondPoints.text = "${topUsers[1].points} pts"
        loadAvatar(secondAvatar, topUsers[1].avatarUrl)

        // Third place
        thirdName.text = topUsers[2].name
        thirdPoints.text = "${topUsers[2].points} pts"
        loadAvatar(thirdAvatar, topUsers[2].avatarUrl)
    }

    private fun loadLeaderboardData() {
        // Mock data - replace with actual data loading
        val leaderboardUsers = listOf(
            User("4", "David Martinez", null, 1850, 4),
            User("5", "Olivia Brown", null, 1720, 5),
            User("6", "James Lee", null, 1650, 6),
            User("7", "Sophia Garcia", null, 1580, 7),
            User("8", "Lucas Anderson", null, 1490, 8),
            User("9", "Isabella Taylor", null, 1420, 9),
            User("10", "Ethan White", null, 1350, 10),
            User("11", "Mia Thompson", null, 1280, 11)
        )

        adapter.submitList(leaderboardUsers)
    }

    private fun loadAvatar(imageView: ImageView, avatarUrl: String?) {
        if (avatarUrl != null) {
            Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_person)
        }
    }
}

