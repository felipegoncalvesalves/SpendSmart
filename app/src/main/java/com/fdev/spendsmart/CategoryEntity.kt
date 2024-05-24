package com.fdev.spendsmart

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
    @ColumnInfo(name = "color")
    val color: Int,
    @ColumnInfo(name = "icon")
    val icon: Int
)
