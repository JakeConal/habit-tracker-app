package com.example.habittracker.ui.progress.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentHabitListTabBinding
import com.example.habittracker.ui.progress.adapter.HabitStatAdapter
import com.example.habittracker.ui.progress.adapter.HabitStatItem

class HabitListTabFragment : Fragment() {

    private var _binding: FragmentHabitListTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HabitStatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadDummyData()
    }

    private fun setupRecyclerView() {
        adapter = HabitStatAdapter()
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HabitListTabFragment.adapter
        }
    }

    private fun loadDummyData() {
        val items = listOf(
            HabitStatItem(
                name = "Praying Namaz",
                score = "Habit Score: 85%",
                iconRes = R.drawable.ic_person,
                iconBgColor = ContextCompat.getColor(requireContext(), R.color.icon_bg_pink),
                badgeText = "Weekday",
                badgeColor = ContextCompat.getColor(requireContext(), R.color.badge_7_days_cyan)
            ),
            HabitStatItem(
                name = "Walk",
                score = "Habit Score: 72%",
                iconRes = R.drawable.ic_walk,
                iconBgColor = ContextCompat.getColor(requireContext(), R.color.icon_bg_pink),
                badgeText = "Weekday",
                badgeColor = ContextCompat.getColor(requireContext(), R.color.badge_7_days_cyan)
            ),
            HabitStatItem(
                name = "Drink The Water",
                score = "Habit Score: 60%",
                iconRes = R.drawable.ic_water,
                iconBgColor = ContextCompat.getColor(requireContext(), R.color.icon_bg_pink),
                badgeText = "Weekday",
                badgeColor = ContextCompat.getColor(requireContext(), R.color.badge_7_days_cyan)
            )
        )
        adapter.setItems(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HabitListTabFragment()
    }
}

