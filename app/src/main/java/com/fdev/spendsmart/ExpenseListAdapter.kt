package com.fdev.spendsmart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ExpenseListAdapter :
    ListAdapter<ExpenseUiData, ExpenseListAdapter.ExpenseViewHolder>(ExpenseListAdapter) {
    private lateinit var callback: (ExpenseUiData) -> Unit
    fun setOnclickListener(onClick: (ExpenseUiData) -> Unit) {
        this.callback = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, callback)
    }

    class ExpenseViewHolder(private val view: View) : RecyclerView.ViewHolder(view.rootView) {
        private val tvCategory = view.findViewById<TextView>(R.id.tv_category_name)
        private val imColorCategory = view.findViewById<ImageView>(R.id.color_Button)
        private val imIconCategory = view.findViewById<ImageView>(R.id.category_icon_expense)
        private val tvExpense = view.findViewById<TextView>(R.id.tv_expense_name)
        private val priceExpense = view.findViewById<TextView>(R.id.tv_expense_price)


        fun bind(expense: ExpenseUiData,
                 callback: (ExpenseUiData) ->Unit ) {
            tvCategory.text = expense.category
            tvExpense.text = expense.name
            priceExpense.text = expense.price
            imColorCategory.setImageResource(expense.color)
            imIconCategory.setImageResource(expense.icon)
            view.setOnClickListener{
                callback.invoke(expense)
            }
        }
    }

    companion object : DiffUtil.ItemCallback<ExpenseUiData>() {
        override fun areItemsTheSame(oldItem: ExpenseUiData, newItem: ExpenseUiData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ExpenseUiData, newItem: ExpenseUiData): Boolean {
            return oldItem.name == newItem.name
        }

    }


}