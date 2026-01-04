package com.example.habittracker.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemCategoryBinding
import com.example.habittracker.data.model.Category

/**
 * CategoryAdapter - Adapter for displaying categories in a list
 */
class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: ((Category) -> Unit)? = null,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

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

                // Set click listener for category selection
                root.setOnClickListener {
                    onCategoryClick?.invoke(category)
                }
                
                // Set click listeners
                btnEdit.setOnClickListener {
                    onEditClick(category)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(category)
                }
            }
        }
    }
}
