package com.example.habittracker.ui.social

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.habittracker.ui.challenge.ChallengesFragment
import com.example.habittracker.ui.leaderboard.LeaderboardFragment
import com.example.habittracker.ui.feed.FeedFragment

/**
 * Adapter cho ViewPager2 trong CommunityFragment
 * Sử dụng Fragment làm parent để hỗ trợ nested fragments
 */
class CommunityPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> FeedFragment()
        1 -> ChallengesFragment()
        2 -> LeaderboardFragment()
        else -> throw IllegalStateException("Invalid position: $position")
    }
}
