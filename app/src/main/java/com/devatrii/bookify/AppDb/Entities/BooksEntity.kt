package com.devatrii.bookify.AppDb.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devatrii.bookify.Models.BooksModel

@Entity(tableName = "books")
data class BooksEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val book_id: String,
    val bookPDF: String,
    val title: String,
    val image: String = "",
    val description: String = "",
    val author: String = "",
    val downloadId: Long,
)