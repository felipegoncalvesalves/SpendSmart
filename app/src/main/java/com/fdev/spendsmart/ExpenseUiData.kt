package com.fdev.spendsmart


data class ExpenseUiData(
    val id: Long,
    val name: String,
    val price: String,
    val category: String,
    val icon: Int,
    val color: Int?
)
