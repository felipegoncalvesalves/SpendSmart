package com.fdev.spendsmart.category

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fdev.spendsmart.R

class CategoryListAdapter :
    ListAdapter<CategoryUiData, CategoryListAdapter.CategoryViewHolder>(CategoryListAdapter) {

    private lateinit var onClick: (CategoryUiData) -> Unit
    private lateinit var onLongClick: (CategoryUiData) -> Unit
    private val allCategoryViews = mutableListOf<View>()

    fun setOnClickListener(onClick: (CategoryUiData) -> Unit) {
        this.onClick = onClick
    }

    fun setOnLongClickListener(onLongClick: (CategoryUiData) -> Unit) {
        this.onLongClick = onLongClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onLongClick, onClick, allCategoryViews)
    }

    inner class CategoryViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory = view.findViewById<TextView>(R.id.tv_category)
        private val ivCategoryIcon: ImageView = view.findViewById(R.id.category_icon)

        fun bind(category: CategoryUiData,
                 onLongClick: (CategoryUiData) -> Unit,
                 onClick: (CategoryUiData) -> Unit,
                 allCategoryViews: MutableList<View>) {

            tvCategory.text = category.name
            tvCategory.isSelected = category.isSelected

            if (category.icon != null && category.icon != 0) {
                try {
                    val drawable = view.context.getDrawable(category.icon)
                    ivCategoryIcon.setImageDrawable(drawable)
                    ivCategoryIcon.visibility = View.VISIBLE
                } catch (e: Resources.NotFoundException) {
                    ivCategoryIcon.visibility = View.GONE
                }
            } else {
                ivCategoryIcon.visibility = View.GONE
            }

            if (!allCategoryViews.contains(view)) {
                allCategoryViews.add(view)
            }

            view.setOnClickListener {
                onClick.invoke(category)
                highlightSelectedCategory(view, allCategoryViews)
            }

            view.setOnLongClickListener {
                onLongClick.invoke(category)
                true
            }
        }

        private fun highlightSelectedCategory(selectedCategoryView: View, allCategoryViews: List<View>) {
            for (view in allCategoryViews) {
                val iconView = view.findViewById<ImageView>(R.id.category_icon)
                iconView.setBackgroundResource(android.R.color.transparent)
            }
            val selectedIconView = selectedCategoryView.findViewById<ImageView>(R.id.category_icon)
            selectedIconView.setBackgroundResource(R.drawable.filter_chips_background)
        }
    }

    companion object : DiffUtil.ItemCallback<CategoryUiData>() {
        override fun areItemsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
