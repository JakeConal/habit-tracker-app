package com.example.habittracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemHabitBinding

class HabitsAdapter(
    private val habits: List<Habit>,
    private val onHabitClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                tvHabitName.text = habit.name
                ivHabitIcon.setImageResource(habit.iconRes)
                habitIconBackground.setBackgroundResource(habit.iconBackgroundRes)

                if (habit.isCompleted) {
                    tvHabitStatus.text = itemView.context.getString(R.string.habit_completed)
                    tvHabitStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.habit_completed)
                    )
                    btnCheck.setBackgroundResource(R.drawable.bg_check_button)
                    ivCheck.visibility = android.view.View.VISIBLE
                } else {
                    if (habit.progress != null) {
                        tvHabitStatus.text = habit.progress
                        tvHabitStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.accent_light_gray)
                        )
                    } else {
                        tvHabitStatus.text = itemView.context.getString(R.string.habit_pending)
                        tvHabitStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.accent_light_gray)
                        )
                    }
                    btnCheck.setBackgroundResource(R.drawable.bg_check_button_empty)
                    ivCheck.visibility = android.view.View.GONE
                }

                btnCheck.setOnClickListener {
                    onHabitClick(habit)
                }
            }
        }
    }

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
}
