package com.devatrii.bookify.Views.Fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.devatrii.bookify.Adapters.HomeChildAdapter
import com.devatrii.bookify.Adapters.SearchAdapter
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.R
import com.devatrii.bookify.Repository.MainRepo
import com.devatrii.bookify.Utils.FirebaseResponse
import com.devatrii.bookify.ViewModels.MainViewModel
import com.devatrii.bookify.ViewModels.MainViewModelFactory
import com.devatrii.bookify.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val binding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }

    private val mainRepo by lazy {
        MainRepo(requireContext())
    }
    private val viewModel by lazy {
        ViewModelProvider(
            (requireContext() as ViewModelStoreOwner),
            MainViewModelFactory(mainRepo)
        )[MainViewModel::class.java]
    }
    private val TAG = "SearchFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {


            mSearch.setOnClickListener {
                val value = editText.text.toString()
                if (value.isEmpty()) {
                    return@setOnClickListener
                }
                viewModel.searchBook(value)
                Toast.makeText(requireContext(), "Searching...", Toast.LENGTH_SHORT).show()
            }
            val list = ArrayList<BooksModel>()
            val adapter = SearchAdapter(list, requireContext())
            mRvChildBooks.adapter = adapter
            viewModel.searchLiveData.observe((requireActivity() as LifecycleOwner)) {
                when (it) {
                    is FirebaseResponse.Error -> {
                        Log.d(TAG, "onCreateView: Error ${it.errorMessage}")
                    }

                    is FirebaseResponse.Loading -> {
                        Log.i(TAG, "onCreateView: Loading")
                    }

                    is FirebaseResponse.Success -> {
                        val data = it.data!!
                        list.clear()
                        data.forEach { model ->
                            list.add(model)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        }



        return binding.root
    }

}