package com.fdev.spendsmart

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fdev.spendsmart.category.CategoryDao
import com.fdev.spendsmart.category.CategoryEntity

@Database([CategoryEntity::class, ExpenseEntity::class], version = 12)
abstract class ExpenseDatabase: RoomDatabase() {

    abstract fun getCategoryDao(): CategoryDao

    abstract fun getExpenseDao(): ExpenseDao
}