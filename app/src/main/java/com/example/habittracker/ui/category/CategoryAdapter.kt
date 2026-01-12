package com.example.habittracker.ui.category

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemCategoryBinding
import com.example.habittracker.data.model.Category

/**
 * CategoryAdapter - Adapter for displaying categories in a list
 */
class CategoryAdapter(
    private var categories: MutableList<Category>,
    private val onCategoryClick: ((Category) -> Unit)? = null,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    companion object {
        private const val TAG = "CategoryAdapter"
    }

    /**
     * Update the categories list and notify the adapter
     */
    fun updateCategories(newCategories: MutableList<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                // Set category name and habit count
                tvCategoryName.text = category.title
                tvHabitCount.text = "${category.habitCount} Habits"

                // Set icon
                ivCategoryIcon.setImageResource(category.icon.resId)

                // Set background
                categoryIconBackground.setBackgroundResource(category.color.resId)

                // Set click listener for category selection on the dedicated selectable area
                // This area is separate from the action buttons, so no event conflict
                categorySelectableArea.setOnClickListener {
                    Log.d(TAG, "categorySelectableArea clicked for: ${category.title}")
                    onCategoryClick?.invoke(category)
                }

                // Set click listener for Edit button
                btnEdit.setOnClickListener {
                    Log.d(TAG, "btnEdit clicked for: ${category.title}")
                    onEditClick(category)
                }

                // Set click listener for Delete button
                btnDelete.setOnClickListener {
                    Log.d(TAG, "btnDelete clicked for: ${category.title}")
                    onDeleteClick(category)
                }
            }
        }
    }
}


