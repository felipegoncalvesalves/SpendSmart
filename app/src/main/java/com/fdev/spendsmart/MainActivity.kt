package com.fdev.spendsmart

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var expensesEntity = listOf<ExpenseEntity>()
    private var expenses = listOf<ExpenseUiData>()
    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var ctnEmptyExpense: LinearLayout
    private lateinit var fabCreateExpense: FloatingActionButton
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
        window.statusBarColor = Color.parseColor("#e57e6c")
        setContentView(R.layout.activity_main)

        rvCategory = findViewById(R.id.rv_categories)
        ctnEmptyView = findViewById(R.id.ll_empty_view)
        ctnEmptyExpense = findViewById(R.id.ll_empty_task)
        val rvExpense = findViewById<RecyclerView>(R.id.rv_expenses)
        fabCreateExpense = findViewById(R.id.fab_create_task)
        val btnCreateEmpty = findViewById<Button>(R.id.btn_create_empty)
        btnCreateEmpty.setOnClickListener {
            showCreateCategoryBottomSheet()
        }

        fabCreateExpense.setOnClickListener {
            showCreateUpdateExpenseBottomSheet()
        }
        expenseAdapter.setOnclickListener { expense ->
            showCreateUpdateExpenseBottomSheet(expense)
        }
        categoryAdapter.setOnClickListener { categoryToBeDelete ->
            if (categoryToBeDelete.name != "+" && categoryToBeDelete.name != "ALL") {
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
                        categoryToBeDelete.color,
                        categoryToBeDelete.icon
                    )
                    deleteCategory(categoryEntityToBeDelete)
                }
            }
        }

        categoryAdapter.setOnLongClickListener { selected ->
            if (selected.name == "+") {
                showCreateCategoryBottomSheet()
            } else {
                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                if (selected.name != "ALL") {
                    filterExpenseByCategoryName(selected.name)
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        getExpensesFromDataBase()
                    }
                }
                categoryAdapter.submitList(categoryTemp)
            }
        }

        rvCategory.adapter = categoryAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }

        rvExpense.adapter = expenseAdapter

        GlobalScope.launch(Dispatchers.IO) {
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

    private fun getCategoriesFromDataBase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        categoriesEntity = categoriesFromDb

        GlobalScope.launch(Dispatchers.Main) {
            if (categoriesEntity.isEmpty()) {
                rvCategory.isVisible = false
                fabCreateExpense.isVisible = false
                ctnEmptyView.isVisible = true
                ctnEmptyExpense.isVisible = false
            } else if (categoriesEntity.isNotEmpty() && expensesEntity.isEmpty()) {
                rvCategory.isVisible = true
                fabCreateExpense.isVisible = true
                ctnEmptyView.isVisible = false
                ctnEmptyExpense.isVisible = true
            } else {
                rvCategory.isVisible = true
                fabCreateExpense.isVisible = true
                ctnEmptyView.isVisible = false
                ctnEmptyExpense.isVisible = false
            }
        }

        val categoriesUiData = categoriesFromDb.map {
            CategoryUiData(
                name = it.name,
                isSelected = it.isSelected,
                color = it.color,
                icon = it.icon
            )
        }.toMutableList()
        categoriesUiData.add(
            CategoryUiData(
                name = "+",
                isSelected = false,
                color = 0,
                icon = 0
            )
        )
        val categoryListTemp = mutableListOf(
            CategoryUiData(
                name = "ALL",
                isSelected = true,
                color = 0,
                icon = 0
            )
        )
        categoryListTemp.addAll(categoriesUiData)
        GlobalScope.launch(Dispatchers.Main) {
            categories = categoryListTemp
            categoryAdapter.submitList(categories)
        }
    }

    private fun getExpensesFromDataBase() {
        val expensesFromDb: List<ExpenseEntity> = expenseDao.getAll()
        expensesEntity = expensesFromDb
        GlobalScope.launch(Dispatchers.Main) {
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
                color = it.color
            )
        }

        GlobalScope.launch(Dispatchers.Main) {
            expenses = expenseUiData
            expenseAdapter.submitList(expenseUiData)
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesFromDataBase()
        }
    }

    private fun insertExpense(expenseEntity: ExpenseEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            expenseDao.insert(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun updateExpense(expenseEntity: ExpenseEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            expenseDao.updata(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun deleteExpense(expenseEntity: ExpenseEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            expenseDao.delete(expenseEntity)
            getExpensesFromDataBase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val expensesToBeDeleted = expenseDao.getAllByCategoryName(categoryEntity.name)
            expenseDao.deleteALL(expensesToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesFromDataBase()
            getExpensesFromDataBase()
        }
    }

    private fun filterExpenseByCategoryName(category: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val expensesFromDb: List<ExpenseEntity> = expenseDao.getAllByCategoryName(category)
            val expensesUiData: List<ExpenseUiData> = expensesFromDb.map {
                ExpenseUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category,
                    icon = it.icon,
                    price = it.price,
                    color = it.color
                )
            }

            GlobalScope.launch(Dispatchers.Main) {
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
                    color = expenseToBeCreated.color
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
                    price = expenseToBeUpdated.price
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
                    price = expenseToBeDeleted.price
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
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName, _, categoryIcon ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                color = Color.BLACK, // Usando a cor padrão preta
                icon = categoryIcon,
                isSelected = false
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }
}
