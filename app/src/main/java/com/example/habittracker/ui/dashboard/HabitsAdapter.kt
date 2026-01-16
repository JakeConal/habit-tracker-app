package com.example.habittracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.Habit
import com.example.habittracker.databinding.ItemHabitBinding

class HabitsAdapter(
    private var habits: MutableList<Habit>,
    private var categories: List<Category>,
    private val onHabitClick: (Habit) -> Unit,
    private val onHabitLongClick: (Habit) -> Unit,
    private val onCheckClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    fun updateHabits(newHabits: MutableList<Habit>, newCategories: List<Category>) {
        habits = newHabits
        categories = newCategories
        notifyDataSetChanged()
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            val category = categories.find { it.id == habit.categoryId }
            val iconRes = category?.icon?.resId ?: R.drawable.ic_other
            val iconBackgroundRes = category?.color?.resId ?: R.drawable.bg_habit_icon_pink

            binding.apply {
                tvHabitName.text = habit.name
                ivHabitIcon.setImageResource(iconRes)
                habitIconBackground.setBackgroundResource(iconBackgroundRes)

                if (habit.isCompleted) {
                    tvHabitStatus.text = itemView.context.getString(R.string.habit_completed)
                    tvHabitStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.habit_completed)
                    )
                    btnCheck.setBackgroundResource(R.drawable.bg_check_button)
                    ivCheck.visibility = android.view.View.VISIBLE
                } else {
                    val progress = if (habit.streak > 0) "${habit.streak} day streak" else null
                    if (progress != null) {
                        tvHabitStatus.text = progress
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

                // Open habit detail when card is tapped, toggle completion when check button is tapped
                root.setOnClickListener { onHabitClick(habit) }
                root.setOnLongClickListener {
                    onHabitLongClick(habit)
                    true
                }
                btnCheck.setOnClickListener { onCheckClick(habit) }
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
