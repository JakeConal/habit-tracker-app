package com.example.habittracker.ui.social.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyPostFragment.newInstance()
            1 -> MyFriendFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
