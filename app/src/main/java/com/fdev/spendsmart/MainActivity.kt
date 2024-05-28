package com.fdev.spendsmart

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.fdev.spendsmart.category.CategoryDao
import com.fdev.spendsmart.category.CategoryEntity
import com.fdev.spendsmart.category.CategoryListAdapter
import com.fdev.spendsmart.category.CategoryUiData
import com.fdev.spendsmart.category.CreateCategoryBottomSheet
import com.fdev.spendsmart.view.CreateOrUpdateExpenseBottomSheet
import com.fdev.spendsmart.view.InfoBottomSheet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var expensesEntity = listOf<ExpenseEntity>()
    private var expenses = listOf<ExpenseUiData>()
    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var ctnEmptyExpense: LinearLayout
    private lateinit var CreateNewExpense: FloatingActionButton
    private lateinit var btnCreateCategory: Button
    private lateinit var ctn_total_expenses : ConstraintLayout
    private val categoryAdapter = CategoryListAdapter()
    private val expenseAdapter by lazy {
        ExpenseListAdapter()
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseDatabase::class.java, "database-expense"
        ).build()
    }

    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val expenseDao: ExpenseDao by lazy {
        db.getExpenseDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#edad9c")
        setContentView(R.layout.activity_main)

        rvCategory = findViewById(R.id.rv_categories)
        ctnEmptyView = findViewById(R.id.ll_empty_view)
        ctnEmptyExpense = findViewById(R.id.ll_empty_expense)
        val rvExpense = findViewById<RecyclerView>(R.id.rv_expenses)
        CreateNewExpense = findViewById(R.id.create_new_expense)
        btnCreateCategory = findViewById(R.id.btn_new_category_create)
        val btnCreateEmpty = findViewById<Button>(R.id.btn_create_empty)
        ctn_total_expenses = findViewById(R.id.container_total_expense)

        btnCreateEmpty.setOnClickListener {
            showCreateCategoryBottomSheet()
        }

        CreateNewExpense.setOnClickListener {
            showCreateUpdateExpenseBottomSheet()
        }

        btnCreateCategory.setOnClickListener {
            showCreateCategoryBottomSheet()
        }

        expenseAdapter.setOnclickListener { expense ->
            showCreateUpdateExpenseBottomSheet(expense)
        }

        categoryAdapter.setOnClickListener { categoryToBeDelete ->
            if (categoryToBeDelete.name != "ALL") {
                val title: String = this.getString(R.string.category_delete_title)
                val description: String = this.getString(R.string.category_delete_description)
                val btnText: String = this.getString(R.string.delete)

                showInfoDialog(
                    title,
                    description,
                    btnText
                ) {
                    val categoryEntityToBeDelete = CategoryEntity(
                        categoryToBeDelete.name,
                        categoryToBeDelete.isSelected,
                        categoryToBeDelete.icon
                    )
                    deleteCategory(categoryEntityToBeDelete)
                }
            }
        }

        categoryAdapter.setOnLongClickListener { selected ->

            if (selected.name != "ALL") {
                filterExpenseByCategoryName(selected.name)
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    getExpensesFromDataBase()
                }
            }

        }

        rvCategory.adapter = categoryAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }

        rvExpense.adapter = expenseAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            getExpensesFromDataBase()
        }
    }

    private fun showInfoDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClick
        )
        infoBottomSheet.show(
            supportFragmentManager,
            "infoBottomSheet"
        )
    }

    private suspend fun getCategoriesFromDataBase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        categoriesEntity = categoriesFromDb

        withContext(Dispatchers.Main) {
            if (categoriesEntity.isEmpty()) {
                rvCategory.isVisible = false
                CreateNewExpense.isVisible = false
                ctn_total_expenses.isVisible = false
                ctnEmptyView.isVisible = true
                ctnEmptyExpense.isVisible = false
            } else if (categoriesEntity.isNotEmpty() && expensesEntity.isEmpty()) {
                rvCategory.isVisible = true
                CreateNewExpense.isVisible = true
                ctnEmptyView.isVisible = false
                ctn_total_expenses.isVisible = true
                ctnEmptyExpense.isVisible = true
            } else {
                rvCategory.isVisible = true
                CreateNewExpense.isVisible = true
                ctnEmptyView.isVisible = false
                ctn_total_expenses.isVisible = true
                ctnEmptyExpense.isVisible = false
            }
        }

        val categoriesUiData = categoriesFromDb.map {
            CategoryUiData(
                name = it.name,
                isSelected = it.isSelected,
                icon = it.icon
            )
        }.toMutableList()

        val categoryListTemp = mutableListOf(
            CategoryUiData(
                name = "ALL",
                isSelected = true,
                icon =  R.drawable.ic_all
            )
        )
        categoryListTemp.addAll(categoriesUiData)

        withContext(Dispatchers.Main) {
            categories = categoryListTemp
            categoryAdapter.submitList(categories)
        }
    }

    private suspend fun getExpensesFromDataBase() {
        val expensesFromDb: List<ExpenseEntity> = expenseDao.getAll()
        expensesEntity = expensesFromDb

        val totalPrice = expensesFromDb.sumOf { expense ->
            val cleanPrice = expense.price.replace("R$", "").replace(".", "").trim()
            val sanitizedPrice = cleanPrice.replace(Regex("[^\\d,]"), "")
            sanitizedPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
        }

        withContext(Dispatchers.Main) {
            val formattedTotalPrice = "R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalPrice)}"
            findViewById<TextView>(R.id.tv_total_espenses).text = formattedTotalPrice

            if (categoriesEntity.isNotEmpty() && expensesEntity.isEmpty()) {
                ctnEmptyExpense.isVisible = true
            } else {
                ctnEmptyExpense.isVisible = false
            }
        }

        val expenseUiData: List<ExpenseUiData> = expensesFromDb.map {
            ExpenseUiData(
                id = it.id,
                name = it.name,
                category = it.category,
                icon = it.icon,
                price = it.price,
                color = it.color,
            )
        }

        withContext(Dispatchers.Main) {
            expenses = expenseUiData
            expenseAdapter.submitList(expenseUiData)
        }
    }


    private fun insertCategory(categoryEntity: CategoryEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesFromDataBase()
        }
    }

    private fun insertExpense(expenseEntity: ExpenseEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            expenseDao.insert(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun updateExpense(expenseEntity: ExpenseEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            expenseDao.updata(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun deleteExpense(expenseEntity: ExpenseEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            expenseDao.delete(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val expensesToBeDeleted = expenseDao.getAllByCategoryName(categoryEntity.name)
            expenseDao.deleteALL(expensesToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesFromDataBase()
            getExpensesFromDataBase()
        }
    }

    private fun filterExpenseByCategoryName(category: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val expensesFromDb: List<ExpenseEntity> = expenseDao.getAllByCategoryName(category)
            val expensesUiData: List<ExpenseUiData> = expensesFromDb.map {
                ExpenseUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category,
                    icon = it.icon,
                    price = it.price,
                    color = it.color,
                )
            }

            withContext(Dispatchers.Main) {
                expenseAdapter.submitList(expensesUiData)
            }
        }
    }

    private fun showCreateUpdateExpenseBottomSheet(expenseUiData: ExpenseUiData? = null) {
        val createExpenseBottomSheet = CreateOrUpdateExpenseBottomSheet(
            expense = expenseUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { expenseToBeCreated ->
                val expenseEntityToBeInserted = ExpenseEntity(
                    name = expenseToBeCreated.name,
                    category = expenseToBeCreated.category,
                    icon = expenseToBeCreated.icon,
                    price = expenseToBeCreated.price,
                    color = expenseToBeCreated.color,
                )
                insertExpense(expenseEntityToBeInserted)
            },
            onUpdateClicked = { expenseToBeUpdated ->
                val expenseEntityToBeUpdated = ExpenseEntity(
                    id = expenseToBeUpdated.id,
                    name = expenseToBeUpdated.name,
                    category = expenseToBeUpdated.category,
                    icon = expenseToBeUpdated.icon,
                    color = expenseToBeUpdated.color,
                    price = expenseToBeUpdated.price ,
                )
                updateExpense(expenseEntityToBeUpdated)
            },
            onDeleteClicked = { expenseToBeDeleted ->
                val expenseEntityToBeDeleted = ExpenseEntity(
                    id = expenseToBeDeleted.id,
                    name = expenseToBeDeleted.name,
                    category = expenseToBeDeleted.category,
                    icon = expenseToBeDeleted.icon,
                    color = expenseToBeDeleted.color,
                    price = expenseToBeDeleted.price,
                )
                deleteExpense(expenseEntityToBeDeleted)
            }
        )
        createExpenseBottomSheet.show(
            supportFragmentManager,
            "createExpenseBottomSheet"
        )
    }

    private fun showCreateCategoryBottomSheet() {
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName, categoryIcon ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                icon = categoryIcon,
                isSelected = false
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }
}
