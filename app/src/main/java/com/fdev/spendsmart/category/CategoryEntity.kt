package com.fdev.spendsmart.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo( "key", )
    val name: String,
    @ColumnInfo("is_selected")
    val isSelected: Boolean,
    @ColumnInfo(name = "icon")
    val icon: Int
)
