package com.fdev.spendsmart

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {
    @Query("Select * From expenseentity")
    fun getAll(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(expenseEntities: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(expenseEntities: ExpenseEntity)

    @Update
    fun updata(expenseEntities:  ExpenseEntity)

    @Delete
    fun delete(expenseEntities: ExpenseEntity)

    @Query("Select * From expenseentity where category is :categoryName")
    fun getAllByCategoryName(categoryName: String): List<ExpenseEntity>

    @Delete
    fun deleteALL(expenseEntities: List<ExpenseEntity>)
}