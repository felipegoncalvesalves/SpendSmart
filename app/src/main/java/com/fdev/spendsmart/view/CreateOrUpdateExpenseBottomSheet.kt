package com.fdev.spendsmart.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdev.spendsmart.ExpenseUiData
import com.fdev.spendsmart.R
import com.fdev.spendsmart.category.CategoryEntity
import com.fdev.spendsmart.category.CategoryListAdapter
import com.fdev.spendsmart.category.CategoryUiData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class CreateOrUpdateExpenseBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val expense: ExpenseUiData? = null,
    private val onCreateClicked: (ExpenseUiData) -> Unit,
    private val onUpdateClicked: (ExpenseUiData) -> Unit,
    private val onDeleteClicked: (ExpenseUiData) -> Unit,
    private val DEFAULT_COLOR: Int = Color.BLACK
) : BottomSheetDialogFragment() {

    private lateinit var categoryAdapter: CategoryListAdapter
    private var selectedCategory: CategoryUiData? = null
    private var selectedColor: Int = DEFAULT_COLOR
    private lateinit var colorButton: ImageView
    private lateinit var categoryIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_or_update_task_bottom_sheet, container, false)
        val viewItem = inflater.inflate(R.layout.item_expense, container, false)


        val tvTitle = view.findViewById<TextView>(R.id.tv_title_total)
        val btnCreateOrUpdate = view.findViewById<Button>(R.id.btn_expense_create_or_updata)
        val btnDelete = view.findViewById<Button>(R.id.btn_expense_delete)
        val tieExpenseName = view.findViewById<TextInputEditText>(R.id.tie_expense_name)
        val tieExpensePrice = view.findViewById<TextInputEditText>(R.id.tie_price)
        val rvCategories: RecyclerView = view.findViewById(R.id.category_recyclerView_list)
        colorButton = viewItem.findViewById(R.id.color_Button)
        categoryIcon = viewItem.findViewById(R.id.category_icon_expense)

        tieExpensePrice.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    tieExpensePrice.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[R$,.]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = String.format(Locale("pt", "BR"), "%, .2f", parsed / 100).replace(" ", "")

                        current = "R$ $formatted"
                        tieExpensePrice.setText(current)
                        tieExpensePrice.setSelection(current.length)
                    } else {
                        current = ""
                    }

                    tieExpensePrice.addTextChangedListener(this)
                }
            }
        })
        val colorButtons = listOf(
            view.findViewById<AppCompatButton>(R.id.blue_Button),
            view.findViewById<AppCompatButton>(R.id.purpleButton),
            view.findViewById<AppCompatButton>(R.id.blackButton),
            view.findViewById<AppCompatButton>(R.id.redButton),
            view.findViewById<AppCompatButton>(R.id.greenButton),
            view.findViewById<AppCompatButton>(R.id.yellowButton),
            view.findViewById<AppCompatButton>(R.id.grayButton)
        )

        val colors = listOf(
            R.drawable.color_blue,
            R.drawable.color_purple,
            R.drawable.color_black,
            R.drawable.color_red,
            R.drawable.color_green,
            R.drawable.color_yellow,
            R.drawable.color_gray
        )


        for ((index, button) in colorButtons.withIndex()) {
            button.setOnClickListener {
                selectedColor = colors[index]
                updateSelectedColorButton(button, colorButtons)
                updateColorButtonTint(selectedColor)
            }
        }

        categoryAdapter = CategoryListAdapter().apply {
            setOnClickListener { category ->
                selectedCategory = category
            }
            setOnLongClickListener {   category ->
            }
        }

        rvCategories.adapter = categoryAdapter
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val categoriesUiData = categoryList.map {
            CategoryUiData(it.name, it.isSelected, it.icon)
        }
        categoryAdapter.submitList(categoriesUiData)


        if (expense != null) {
            val currentCategory = categoriesUiData.firstOrNull { it.name == expense.category && it.icon == expense.icon }
            selectedCategory = currentCategory
            currentCategory?.isSelected = true
            tieExpenseName.setText(expense.name)
            tieExpensePrice.setText(expense.price)
            selectedColor = expense.color
            updateColorButtonTint(selectedColor)
            colorButton.setBackgroundResource(selectedColor)

            val selectedCategoryIcon = viewItem.findViewById<ImageView>(R.id.category_icon_expense)
            selectedCategoryIcon.setImageResource(expense.icon)


            btnDelete.isVisible = true
            tvTitle.setText(R.string.update_expense_title)
            btnCreateOrUpdate.setText(R.string.update)
        } else {
            btnDelete.isVisible = false
            tvTitle.setText(R.string.create_expense_title)
            btnCreateOrUpdate.setText(R.string.create)
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
            val color = selectedColor

            if (selectedCategory != null && name.isNotEmpty() && price.isNotEmpty()) {
                val expenseData = ExpenseUiData(
                    id = expense?.id ?: 0,
                    name = name,
                    category = selectedCategory!!.name,
                    icon = selectedCategory!!.icon,
                    price = price,
                    color = color,
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

    private fun updateColorButtonTint(color: Int) {
        colorButton.imageTintList = ColorStateList.valueOf(color)
    }
    private fun updateSelectedColorButton(selectedButton: AppCompatButton, allButtons: List<AppCompatButton>) {
        for (button in allButtons) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
    }
}
