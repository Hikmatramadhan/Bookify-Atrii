package com.devatrii.bookify.AppDb.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("notes")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imagePath: String = "",
    val note: String = "",
    val pageNo: Int = 0,
    val book_id: String = ""
)
