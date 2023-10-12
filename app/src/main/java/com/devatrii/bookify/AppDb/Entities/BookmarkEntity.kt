package com.devatrii.bookify.AppDb.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val book_id: String = "",
    val pageNo: Int = 0
)