package com.devatrii.bookify.AppDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devatrii.bookify.AppDb.Dao.BookmarkDao
import com.devatrii.bookify.AppDb.Dao.BooksDao
import com.devatrii.bookify.AppDb.Dao.NotesDao
import com.devatrii.bookify.AppDb.Entities.BookmarkEntity
import com.devatrii.bookify.AppDb.Entities.BooksEntity
import com.devatrii.bookify.AppDb.Entities.NotesEntity

@Database(entities = [BooksEntity::class,BookmarkEntity::class,NotesEntity::class], version = 1)

abstract class AppDatabase : RoomDatabase() {

    abstract fun booksDao(): BooksDao
    abstract fun bookmarkDao():BookmarkDao
    abstract fun notesDao():NotesDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "Bookify_Books"
                    ).build()
                }
            }
            return INSTANCE!!
        }

    }

}