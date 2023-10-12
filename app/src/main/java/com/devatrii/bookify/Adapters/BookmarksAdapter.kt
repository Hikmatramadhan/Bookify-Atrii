package com.devatrii.bookify.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.devatrii.bookify.AppDb.Entities.BookmarkEntity
import com.devatrii.bookify.ViewModels.PageScrollViewModel
import com.devatrii.bookify.databinding.ItemBookmarksBinding

class BookmarksAdapter(val list: ArrayList<BookmarkEntity>, val context: Context) :
    RecyclerView.Adapter<BookmarksAdapter.ChildViewHolder>() {

  inner  class ChildViewHolder(val binding: ItemBookmarksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val pageScrollViewModel by lazy {
            ViewModelProvider(context as ViewModelStoreOwner)[PageScrollViewModel::class.java]
        }
        fun bind(model: BookmarkEntity) {
            binding.apply {
                model.apply {
                    mPageBtn.text = "${pageNo+1}"
                    mPageBtn.setOnClickListener {
                        // soon
                        pageScrollViewModel.jumpTo(pageNo,true)
                    }

                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(ItemBookmarksBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(list[position])
    }
}