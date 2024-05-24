package com.fdev.spendsmart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateOrUpdateExpenseBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val expense: ExpenseUiData? = null,
    private val onCreateClicked: (ExpenseUiData) -> Unit,
    private val onUpdateClicked: (ExpenseUiData) -> Unit,
    private val onDeleteClicked: (ExpenseUiData) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var categoryAdapter: CategoryListAdapter
    private var selectedCategory: CategoryUiData? = null
    private var selectedCategoryColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_or_update_task_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val btnCreateOrUpdate = view.findViewById<Button>(R.id.btn_expense_create_or_updata)
        val btnDelete = view.findViewById<Button>(R.id.btn_expense_delete)
        val tieExpenseName = view.findViewById<TextInputEditText>(R.id.tie_expense_name)
        val tieExpensePrice = view.findViewById<TextInputEditText>(R.id.tie_price)
        val rvCategories: RecyclerView = view.findViewById(R.id.category_recyclerView_list)
        val blueButton = view.findViewById<AppCompatButton>(R.id.blueButton)
        val purpleButton = view.findViewById<AppCompatButton>(R.id.purpleButton)
        val blackButton = view.findViewById<AppCompatButton>(R.id.blackButton)
        val redButton = view.findViewById<AppCompatButton>(R.id.redButton)
        val greenButton = view.findViewById<AppCompatButton>(R.id.greenButton)
        val yellowButton = view.findViewById<AppCompatButton>(R.id.yellowButton)

        blueButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.blue)
        }
        purpleButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.purple)
        }

        blackButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        redButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.red)
        }

        greenButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.green)
        }

        yellowButton.setOnClickListener {
            selectedCategoryColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        }

        categoryAdapter = CategoryListAdapter().apply {
            setOnClickListener { category ->
                selectedCategory = category
                notifyDataSetChanged()
            }
            setOnLongClickListener { category ->
            }
        }

        rvCategories.adapter = categoryAdapter
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


        val categoriesUiData = categoryList.map {
            CategoryUiData(it.name, it.isSelected, it.color, it.icon)
        }
        categoryAdapter.submitList(categoriesUiData)

        if (expense != null) {
            val currentCategory = categoriesUiData.firstOrNull { it.name == expense.category && it.icon == expense.icon }
            selectedCategory = currentCategory
            currentCategory?.isSelected = true
        }

        if (expense == null) {
            btnDelete.isVisible = false
            tvTitle.setText(R.string.create_expense_title)
            btnCreateOrUpdate.setText(R.string.create)
        } else {
            tvTitle.setText(R.string.update_expense_title)
            btnCreateOrUpdate.setText(R.string.update)
            tieExpenseName.setText(expense.name)
            tieExpensePrice.setText(expense.price)

            btnDelete.isVisible = true

        }

        btnDelete.setOnClickListener {
            if (expense != null) {
                onDeleteClicked.invoke(expense)
                dismiss()
            } else {
                Log.d("CreateOrUpdateExpenseBottomSheet", "Expense not found")
            }
        }

        btnCreateOrUpdate.setOnClickListener {
            val name = tieExpenseName.text.toString().trim()
            val price = tieExpensePrice.text.toString().trim()


            if (selectedCategory != null && name.isNotEmpty() && price.isNotEmpty()) {
                val expenseData = ExpenseUiData(
                    id = expense?.id ?: 0,
                    name = name,
                    category = selectedCategory!!.name,
                    icon = selectedCategory!!.icon,
                    price = price,
                    color = selectedCategoryColor
                )

                if (expense == null) {
                    onCreateClicked.invoke(expenseData)
                } else {
                    onUpdateClicked.invoke(expenseData)
                }
                dismiss()
            } else {
                Snackbar.make(view, "Please fill all fields", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}
