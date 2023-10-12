package com.devatrii.bookify.Views.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.devatrii.bookify.Adapters.BookmarksAdapter
import com.devatrii.bookify.AppDb.AppDatabase
import com.devatrii.bookify.AppDb.Entities.BookmarkEntity
import com.devatrii.bookify.ViewModels.PageScrollViewModel
import com.devatrii.bookify.databinding.FragmentBookmarksBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BookmarksFragment : BottomSheetDialogFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.NoBackdropBottomSheetDialogTheme)

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private val binding by lazy {
        FragmentBookmarksBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<BookmarkEntity>()
    private val adapter by lazy {
        BookmarksAdapter(list, requireContext())
    }
    private val db by lazy {
        AppDatabase.getDatabase(requireContext())
    }
    private val bookmarkDao by lazy {
        db.bookmarkDao()
    }

    val pageScrollViewModel by lazy {
        ViewModelProvider(activity as ViewModelStoreOwner)[PageScrollViewModel::class.java]
    }
    val TAG = "BOOKFRAGMENT"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val args = arguments
        if (args != null) {
            val value = args.getString("book_id")
            // Use the data as needed
            // Disabled Log Log.i(TAG, "onCreateView: book id $value")
            binding.apply {
                mRecyclerBookMarks.adapter = adapter
                CoroutineScope(Dispatchers.IO).launch {
                    val data = bookmarkDao.getAllBookmarksByBook(value!!)
                    // Disabled Log Log.i(TAG, "onCreateView: data: $data")
                    if (data.isNotEmpty()) {
                        val distinctPageNumbers = data.map { it.pageNo }.distinct()
                        distinctPageNumbers.forEach {
                            list.add(BookmarkEntity(pageNo = it))
                        }
                        withContext(Dispatchers.Main) {
                            adapter.notifyDataSetChanged()
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            textView.text = "Not Found any Bookmarks"
                        }
                    }
                }

                pageScrollViewModel.pageNumberLd.observe(requireActivity() as LifecycleOwner) {
                    if (it.hideDialog) {
                        this@BookmarksFragment.dismiss()
                    }
                }
            }
        }
        return binding.root
    }


}