package com.devatrii.bookify.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.Repository.BookRepo
import com.devatrii.bookify.Repository.DownloadModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookViewModel(val repo: BookRepo):ViewModel() {

    val downloadLiveData get() = repo.downloadLiveData

    fun downloadFile(url: String, fileName: String,id:String,image: String,description:String,author:String) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.downloadPDF(url, fileName,id,image,description, author)
        }
    }
}