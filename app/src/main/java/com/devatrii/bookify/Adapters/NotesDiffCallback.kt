package com.devatrii.bookify.Adapters

import androidx.recyclerview.widget.DiffUtil
import com.devatrii.bookify.AppDb.Entities.NotesEntity

class NotesDiffCallback(
    private val oldList: ArrayList<NotesEntity>,
    private val newList: ArrayList<NotesEntity>
) : DiffUtil.Callback() {


    override fun getOldListSize() = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
       return oldList[oldItemPosition].id === newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldCourse: Int, newPosition: Int): Boolean {
        val (_, value, name) = oldList[oldCourse]
        val (_, value1, name1) = newList[newPosition]
        return name == name1 && value == value1
    }
}
