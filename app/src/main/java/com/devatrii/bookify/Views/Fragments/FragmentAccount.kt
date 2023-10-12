package com.devatrii.bookify.Views.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devatrii.bookify.Adapters.SearchAdapter
import com.devatrii.bookify.AppDb.AppDatabase
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.R
import com.devatrii.bookify.databinding.FragmentAccountBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentAccount : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val binding by lazy {
        FragmentAccountBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val list = ArrayList<BooksModel>()
        val adapter = SearchAdapter(list, requireContext())
        binding.mRvDownloads.adapter = adapter

        val db = activity?.let { AppDatabase.getDatabase(it) }!!
        CoroutineScope(Dispatchers.IO).launch {
            list.clear()
            db.booksDao().getAllBooks().forEach {
//            mRvDownloads
                val model = BooksModel(
                    id = it.book_id,
                    image = it.image,
                    title = it.title,
                    description = it.description,
                    author = it.author,
                    position = -1,
                    bookPDF = it.bookPDF
                )
                list.add(model)
                activity?.runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }

        }

        return binding.root
    }


}