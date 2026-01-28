package com.example.habittracker.ui.progress.tabs

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentStatisticsTabBinding
import com.example.habittracker.ui.progress.StatisticsViewModel
import kotlinx.coroutines.launch

class StatisticsTabFragment : Fragment() {

    private var _binding: FragmentStatisticsTabBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by activityViewModels()
    private var currentWeekData: List<com.example.habittracker.ui.progress.WeeklyChartData> = emptyList()
    private var currentTooltipView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarClickListeners()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when tab becomes visible
        viewModel.loadAllData()
    }

    private fun observeData() {
        // Always show contentLayout
        binding.contentLayout.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.overallCompletionRate.collect { rate ->
                val rateInt = rate.toInt()
                binding.tvHabitScore.text = getString(R.string.percentage_format, rateInt)
                binding.progressCircle.progress = rateInt
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyChartData.collect { weekData ->
                currentWeekData = weekData
                updateWeeklyChart(weekData)
                updateLegend(weekData)
            }
        }
    }

    private fun updateWeeklyChart(weekData: List<com.example.habittracker.ui.progress.WeeklyChartData>) {
        if (weekData.isEmpty()) {
            updateYAxisLabels(0)
            return
        }

        // Find the maximum completion count across all days
        val maxCompletionCount = weekData.maxOfOrNull { it.completionCount } ?: 0

        // Get the Y-axis max value (which may be rounded up)
        val yAxisMax = calculateYAxisMax(maxCompletionCount)
        updateYAxisLabels(yAxisMax)

        // Get all bar views
        val bars = listOf(
            binding.barSun,
            binding.barMon,
            binding.barTue,
            binding.barWed,
            binding.barThu,
            binding.barFri,
            binding.barSat
        )

        // Maximum height for chart bars (in dp)
        val maxHeightDp = 180
        val density = resources.displayMetrics.density

        weekData.forEachIndexed { index, data ->
            if (index < bars.size) {
                val bar = bars[index]

                // Calculate height based on completion count relative to Y-axis max
                val heightDp = if (data.completionCount > 0 && yAxisMax > 0) {
                    ((data.completionCount.toFloat() / yAxisMax) * maxHeightDp).coerceAtLeast(10f)
                } else {
                    10f  // Default minimum height
                }

                // Convert dp to pixels
                val heightPx = (heightDp * density).toInt()

                // Update bar height
                val layoutParams = bar.layoutParams
                layoutParams.height = heightPx
                bar.layoutParams = layoutParams

                // Update alpha based on completion rate (0.4 to 1.0)
                val alpha = if (data.totalHabits > 0) {
                    (0.4f + (data.completionRate / 100f) * 0.6f).coerceIn(0.4f, 1.0f)
                } else {
                    0.3f  // Very faint if no habits scheduled
                }
                bar.alpha = alpha
            }
        }
    }

    private fun calculateYAxisMax(maxValue: Int): Int {
        return when {
            maxValue == 0 -> 10
            maxValue == 1 -> 1
            maxValue <= 5 -> 5
            maxValue <= 10 -> 10
            maxValue <= 20 -> 20
            maxValue <= 50 -> 50
            else -> ((maxValue / 10) + 1) * 10  // Round up to nearest 10
        }
    }

    private fun updateYAxisLabels(yAxisMax: Int) {
        binding.tvYAxisMax.text = yAxisMax.toString()

        // Only show middle label if max > 1
        if (yAxisMax > 1) {
            val axisVal = yAxisMax.toFloat() / 2
            val df = java.text.DecimalFormat("0.#")
            binding.tvYAxisMid.text = df.format(axisVal)
            binding.tvYAxisMid.visibility = View.VISIBLE
        } else {
            binding.tvYAxisMid.visibility = View.GONE
        }

        binding.tvYAxisMin.text = "0"
    }

    private fun updateLegend(weekData: List<com.example.habittracker.ui.progress.WeeklyChartData>) {
        if (weekData.isEmpty()) {
            binding.tvTotalCompletedHabits.text = getString(R.string.total_habits_completed_week, 0)
            binding.tvWeeklyCompletionRate.text = getString(R.string.weekly_completion_rate, 0)
            return
        }

        val totalCompletions = weekData.sumOf { it.completionCount }
        val totalHabits = weekData.sumOf { it.totalHabits }
        val avgCompletionRate = if (totalHabits > 0) {
            (totalCompletions.toFloat() / totalHabits * 100).toInt()
        } else {
            0
        }

        binding.tvTotalCompletedHabits.text = getString(R.string.total_habits_completed_week, totalCompletions)
        binding.tvWeeklyCompletionRate.text = getString(R.string.weekly_completion_rate, avgCompletionRate)
    }

    private fun setupBarClickListeners() {
        val barContainers = listOf(
            binding.barContainerSun,
            binding.barContainerMon,
            binding.barContainerTue,
            binding.barContainerWed,
            binding.barContainerThu,
            binding.barContainerFri,
            binding.barContainerSat
        )

        barContainers.forEachIndexed { index, container ->
            container.setOnClickListener {
                if (index < currentWeekData.size) {
                    showTooltip(container, currentWeekData[index])
                }
            }
        }

        // Hide tooltip when clicking outside
        binding.tooltipContainer.setOnClickListener {
            hideTooltip()
        }
    }

    private fun showTooltip(anchorView: View, data: com.example.habittracker.ui.progress.WeeklyChartData) {
        // Hide existing tooltip if any
        hideTooltip()

        // Inflate tooltip layout
        val tooltipView = LayoutInflater.from(requireContext())
            .inflate(R.layout.tooltip_bar_chart, binding.tooltipContainer, false)

        // Set tooltip data
        val tvCompleted = tooltipView.findViewById<TextView>(R.id.tvTooltipCompleted)
        val tvCompletionRate = tooltipView.findViewById<TextView>(R.id.tvTooltipCompletionRate)

        tvCompleted.text = getString(R.string.completed_habits_format, data.completionCount, data.totalHabits)
        tvCompletionRate.text = getString(R.string.completion_rate_format, data.completionRate.toInt())

        // Calculate tooltip position
        val anchorLocation = IntArray(2)
        anchorView.getLocationInWindow(anchorLocation)

        val containerLocation = IntArray(2)
        binding.tooltipContainer.getLocationInWindow(containerLocation)

        // Measure tooltip to get its size
        tooltipView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val tooltipWidth = tooltipView.measuredWidth
        val tooltipHeight = tooltipView.measuredHeight

        // Position tooltip above the bar, centered
        val x = anchorLocation[0] - containerLocation[0] + (anchorView.width / 2) - (tooltipWidth / 2)
        val y = anchorLocation[1] - containerLocation[1] - tooltipHeight - 16 // 16dp margin

        // Add tooltip to container
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.leftMargin = x.coerceAtLeast(0) // Keep within bounds with 8dp padding
        layoutParams.topMargin = y.coerceAtLeast(0)
        layoutParams.gravity = Gravity.TOP or Gravity.START

        binding.tooltipContainer.addView(tooltipView, layoutParams)
        binding.tooltipContainer.visibility = View.VISIBLE

        currentTooltipView = tooltipView
    }

    private fun hideTooltip() {
        binding.tooltipContainer.removeAllViews()
        binding.tooltipContainer.visibility = View.GONE
        currentTooltipView = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideTooltip()
        _binding = null
    }

    companion object {
        fun newInstance() = StatisticsTabFragment()
    }
}

