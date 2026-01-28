package com.example.habittracker.ui.progress.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentHabitListTabBinding
import com.example.habittracker.ui.habit.detail.ViewHabitDetailActivity
import com.example.habittracker.ui.progress.StatisticsViewModel
import com.example.habittracker.ui.progress.adapter.HabitStatAdapter
import com.example.habittracker.ui.progress.adapter.HabitStatItem
import kotlinx.coroutines.launch

class HabitListTabFragment : Fragment() {

    private var _binding: FragmentHabitListTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HabitStatAdapter
    private val viewModel: StatisticsViewModel by activityViewModels()

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
        observeData()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when tab becomes visible
        viewModel.loadAllData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitStatistics.collect { statistics ->
                if (statistics.isEmpty()) {
                    binding.rvHabits.visibility = View.GONE
                    binding.llEmptyHabits.visibility = View.VISIBLE
                } else {
                    binding.rvHabits.visibility = View.VISIBLE
                    binding.llEmptyHabits.visibility = View.GONE
                    val items = statistics.map { stat ->
                        HabitStatItem(
                            habitId = stat.habitId,
                            name = stat.habitName,
                            iconRes = stat.iconRes,
                            iconBgRes = stat.iconBgRes,
                            badgeText = stat.frequency,
                            badgeBgRes = R.drawable.badge_color_cyan,
                            weeklyDays = stat.weeklyDays
                        )
                    }
                    adapter.setItems(items)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = HabitStatAdapter { habitId ->
            navigateToHabitDetail(habitId)
        }
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HabitListTabFragment.adapter
        }
    }

    private fun navigateToHabitDetail(habitId: String) {
        val intent = ViewHabitDetailActivity.newIntent(requireContext(), habitId)
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HabitListTabFragment()
    }
}

