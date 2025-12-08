package com.example.habittracker.ui.progress.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.habittracker.ui.progress.tabs.CalendarTabFragment
import com.example.habittracker.ui.progress.tabs.HabitListTabFragment
import com.example.habittracker.ui.progress.tabs.StatisticsTabFragment

class StatisticPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HabitListTabFragment.newInstance()
            1 -> CalendarTabFragment.newInstance()
            2 -> StatisticsTabFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

