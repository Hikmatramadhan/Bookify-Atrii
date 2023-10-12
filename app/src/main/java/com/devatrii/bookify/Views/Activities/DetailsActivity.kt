package com.devatrii.bookify.Views.Activities

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.Repository.BookRepo
import com.devatrii.bookify.Repository.MainRepo
import com.devatrii.bookify.Utils.FirebaseResponse
import com.devatrii.bookify.Utils.loadOnline
import com.devatrii.bookify.ViewModels.BookViewModel
import com.devatrii.bookify.ViewModels.BookViewModelFactory
import com.devatrii.bookify.databinding.ActivityDetailsBinding
import com.devatrii.bookify.databinding.LayoutLoadingBinding
import com.devatrii.bookify.databinding.LayoutProgressBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {
    val activity = this
    private val TAG = "DetailsActivity"
    lateinit var binding: ActivityDetailsBinding

    val dRepo by lazy { BookRepo(activity) }
    val bookViewModel by lazy {
        ViewModelProvider(
            activity, BookViewModelFactory(dRepo)
        )[BookViewModel::class.java]
    }

    private var isClickedDownloaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bookModel = intent.getSerializableExtra("book_model") as BooksModel

        binding.apply {
            if (bookModel == null) {
                // handle error
            }
            CoroutineScope(Dispatchers.IO).launch {
              //  MainRepo(activity).addBook(bookModel)
            }

            bookModel.apply {
                imageView.loadOnline(image)
                mBookTitle.text = title
                mAuthorName.text = author
                mBookDesc.text = description
                supportActionBar?.title = "Book Details"

                mReadBookBtn.setOnClickListener {
                    isClickedDownloaded = false
                    bookViewModel.downloadFile(
                        url = bookModel.bookPDF,
                        fileName = bookModel.title + ".pdf",
                        id = bookModel.id,
                        image = image,
                        description = description,
                        author = author
                    )
                }
                mDownloadBookBtn.setOnClickListener {
                    isClickedDownloaded = true
                    bookViewModel.downloadFile(
                        url = bookModel.bookPDF,
                        fileName = bookModel.title + ".pdf",
                        id = bookModel.id,
                        image = image,
                        description = description,
                        author = author
                    )
                    Toast.makeText(
                        activity, "Downloading don't remove app from background", Toast.LENGTH_SHORT
                    ).show()
                }
                // setup more from author
                val dialogBinding = LayoutProgressBinding.inflate(layoutInflater)
                val dialog = Dialog(activity).apply {
                    setContentView(dialogBinding.root)
                    setCancelable(false)
                    this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    this.window!!.setLayout(
                        ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
                    );
                }


                bookViewModel.downloadLiveData.observe(activity) {
                    Log.i(TAG, "onCreate: $it")
                    when (it) {
                        is FirebaseResponse.Error -> {
                            Log.e(TAG, "onCreate: Error ${it.errorMessage}")
                            dialog.dismiss()
                            Toast.makeText(activity, "${it.errorMessage}", Toast.LENGTH_SHORT)
                                .show()
                        }

                        is FirebaseResponse.Loading -> {
                            Log.i(TAG, "onCreate: Downloading... ${it.progress}%")
                            dialogBinding.mProgress.text = "${it.progress}%"
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dialogBinding.mProgressBar.setProgress(it.progress, true)
                            } else {
                                dialogBinding.mProgressBar.progress = it.progress
                            }
                            if (!isClickedDownloaded) dialog.show()
                        }

                        is FirebaseResponse.Success -> {
                            dialog.dismiss()
                            val model = it.data
                            Log.i(TAG, "Downloaded: ${model}")
                            Intent().apply {
                                putExtra("book_pdf", model?.filePath.toString())
                                setClass(activity, PdfActivity::class.java)
                                if (!isClickedDownloaded) startActivity(this)
                            }
                        }
                    }


                }
            }


        }

    }
}