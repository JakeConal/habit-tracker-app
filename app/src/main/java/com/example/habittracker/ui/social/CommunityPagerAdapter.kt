package com.example.habittracker.ui.social

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.habittracker.ui.challenge.list.ChallengesFragment
import com.example.habittracker.ui.leaderboard.LeaderboardFragment
import com.example.habittracker.ui.social.feed.FeedFragment

class CommunityPagerAdapter (
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> FeedFragment()
        1 -> ChallengesFragment()
        2 -> LeaderboardFragment()
        else -> throw IllegalStateException("Invalid position: $position")
    }
}

