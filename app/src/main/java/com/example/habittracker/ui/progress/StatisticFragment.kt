package com.example.habittracker.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.habittracker.databinding.FragmentStatisticBinding
import com.example.habittracker.ui.progress.adapter.StatisticPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

/**
 * StatisticFragment - Statistics and progress screen with three tabs
 * - All habits: List of habits with weekly progress
 * - Calendar: Monthly calendar view with streak tracking
 * - Statistics: Habit score and weekly chart
 */
class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: StatisticPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupTabLayout()
    }

    private fun setupViewPager() {
        pagerAdapter = StatisticPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All habits"
                1 -> "Calendar"
                2 -> "Statistics"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StatisticFragment()
    }
}

