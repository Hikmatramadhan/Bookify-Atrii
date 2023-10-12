package com.devatrii.bookify.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.devatrii.bookify.Views.Activities.DetailsActivity
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.Utils.loadOnline
import com.devatrii.bookify.databinding.ItemBookBinding
import com.devatrii.bookify.databinding.ItemBookSearchBinding

class SearchAdapter(val list: ArrayList<BooksModel>, val context: Context) :
    RecyclerView.Adapter<SearchAdapter.ChildViewHolder>() {

    class ChildViewHolder(val binding: ItemBookSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BooksModel, context: Context) {
            binding.apply {
                model.apply {
                    imageView.loadOnline(image)
                    cardView.setOnClickListener {
                        // handle on click here
                        Intent().apply {
                            putExtra("book_model", model)
                            setClass(context, DetailsActivity::class.java)
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                context as Activity,
                                cardView,
                                cardView.transitionName
                            )
                            context.startActivity(this, options.toBundle())
                        }

                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(ItemBookSearchBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(list[position], context)
    }
}