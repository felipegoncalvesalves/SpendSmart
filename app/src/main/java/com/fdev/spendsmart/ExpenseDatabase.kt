package com.fdev.spendsmart

import androidx.room.Database
import androidx.room.RoomDatabase

@Database([CategoryEntity::class, ExpenseEntity::class], version = 9)
abstract class ExpenseDatabase: RoomDatabase() {

    abstract fun getCategoryDao(): CategoryDao

    abstract fun getExpenseDao(): ExpenseDao
}