package com.devatrii.bookify.AppDb.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.devatrii.bookify.AppDb.Entities.BookmarkEntity

@Dao
interface BookmarkDao {
    @Insert
    suspend fun insertBookMark(bookmarkEntity: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmarkEntity: BookmarkEntity)

    @Query("Select * from bookmarks")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("Select * from bookmarks WHERE book_id = :bookId")
    suspend fun getAllBookmarksByBook(bookId: String): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE book_id = :bookId AND pageNo = :page_")
    suspend fun getBookmark(bookId: String, page_: Int = 0): BookmarkEntity?

}