package com.devatrii.bookify.AppDb.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.devatrii.bookify.AppDb.Entities.NotesEntity

@Dao
interface NotesDao {
    @Insert
    suspend fun insertNote(notesEntity: NotesEntity)

    @Delete
    suspend fun deleteNote(notesEntity: NotesEntity)

    @Query("Select * from notes")
    suspend fun getAllNotes(): List<NotesEntity>?

    @Query("Select * from notes WHERE book_id = :bookId")
    suspend fun getAllNotesByBook(bookId: String): List<NotesEntity>?

    @Query("SELECT * FROM notes WHERE book_id = :bookId AND pageNo = :page_")
    suspend fun getBookmarkByPage(bookId: String, page_: Int = 0): List<NotesEntity>?

}