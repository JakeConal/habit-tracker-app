package com.example.habittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemHabitBinding

/**
 * RecyclerView adapter for displaying habit items.
 * Follows the ViewHolder pattern and delegates click events to the Activity.
 */
class HabitAdapter(
    private val onCompleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private val habits = mutableListOf<Habit>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    /**
     * Update the entire list of habits and refresh the view
     */
    fun submitList(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    /**
     * Update a single habit item
     */
    fun updateHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            notifyItemChanged(index)
        }
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                habitIcon.text = habit.icon
                habitTitle.text = habit.title

                // Update status text and color based on completion
                if (habit.isCompleted) {
                    habitStatus.text = root.context.getString(R.string.completed)
                    habitStatus.setTextColor(
                        ContextCompat.getColor(root.context, R.color.success)
                    )
                    checkButton.alpha = 0.5f
                    checkButton.isEnabled = false
                } else {
                    habitStatus.text = root.context.getString(R.string.pending)
                    habitStatus.setTextColor(
                        ContextCompat.getColor(root.context, R.color.textSecondary)
                    )
                    checkButton.alpha = 1.0f
                    checkButton.isEnabled = true
                }

                // Handle check button click
                checkButton.setOnClickListener {
                    if (!habit.isCompleted) {
                        onCompleteClick(habit)
                    }
                }
            }
        }
    }
}

