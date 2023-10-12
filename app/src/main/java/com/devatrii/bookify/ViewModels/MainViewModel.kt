package com.devatrii.bookify.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devatrii.bookify.Repository.MainRepo
import com.devatrii.bookify.Utils.generateSubstrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repo: MainRepo) : ViewModel() {
    val homeLiveData get() = repo.homeLiveData
    val searchLiveData get() = repo.booksLiveData

    fun getHomeData() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getHomeData()
        }
    }

    fun searchBook(bookName:String){
        CoroutineScope(Dispatchers.IO).launch {
            val listKeywords = generateSubstrings(bookName)
            Log.i("Main", "searchBook: $listKeywords")
            repo.searchBook(listKeywords)
        }
    }





}