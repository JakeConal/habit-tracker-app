package com.example.habittracker.ui.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.habittracker.ui.fragments.ChallengesFragment
import com.example.habittracker.ui.fragments.FeedFragment
import com.example.habittracker.ui.fragments.LeaderboardFragment

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