package com.devatrii.bookify.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devatrii.bookify.AppDb.AppDatabase
import com.devatrii.bookify.AppDb.Entities.NotesEntity
import com.devatrii.bookify.R
import com.devatrii.bookify.Utils.removeView
import com.devatrii.bookify.Utils.showMessage
import com.devatrii.bookify.Views.Activities.ActivityImage
import com.devatrii.bookify.databinding.ItemNoteImageBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAdapter(val list: ArrayList<NotesEntity>, val context: Context) :
    RecyclerView.Adapter<NotesAdapter.ChildViewHolder>() {


    fun submitList(oldList:ArrayList<NotesEntity>,newList: ArrayList<NotesEntity>) {
        val diffCallback = NotesDiffCallback(oldList, newList)
        val difNotes = DiffUtil.calculateDiff(diffCallback)
        list.clear()
        list.addAll(newList)
        difNotes.dispatchUpdatesTo(this)
    }

    inner class ChildViewHolder(val binding: ItemNoteImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notesDao = AppDatabase.getDatabase(context).notesDao()
        fun bind(model: NotesEntity, context: Context) {
            binding.apply {
                model.apply {
                    if (imagePath.isNotEmpty()) {
                        // Disabled Log Log.i("PDF_VIEW", "bind: Image Path $imagePath")
                        mNoteImage.setImageURI((Uri.parse(imagePath)))
                        mNoteImage.setOnClickListener {
                            Intent().apply {
                                putExtra("image_uri", imagePath)
                                setClass(context, ActivityImage::class.java)
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    context as Activity, mNoteImage, mNoteImage.transitionName
                                )
                                context.startActivity(this, options.toBundle())
                            }
                        }
                    } else {
                        mNoteImage.setImageBitmap(null)
                    }
                    mNote.text = note
                    binding.root.setOnLongClickListener {
                        MaterialAlertDialogBuilder(context).apply {
                            setIcon(R.drawable.ic_notes_delete)
                            setTitle("Delete Note!")
                            setMessage("Are you sure that you want to delete this note?")
                            setPositiveButton("Yes") { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    notesDao.deleteNote(model)
                                    withContext(Dispatchers.Main) {
                                        showMessage("Deleted!", context)
                                        binding.root.removeView()
                                    }
                                }
                            }
                            setNegativeButton("No", null)
                            show()
                        }
                        true
                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(
            ItemNoteImageBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(list[position], context)
    }
}