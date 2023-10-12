package com.devatrii.bookify.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class PageScrollModel(
    val page: Int = 0, val hideDialog: Boolean = false, val scroll: Boolean = true
)

class PageScrollViewModel() : ViewModel() {

    private val _pld = MutableLiveData<PageScrollModel>()
    val pageNumberLd: LiveData<PageScrollModel>
        get() = _pld

    // Function to trigger the observer
    fun jumpTo(page: Int, hideDialog: Boolean) {
        _pld.value = PageScrollModel(page, hideDialog)
        // Disabled Log Log.d("chapters_Adapter", "${_pld.value}")
    }

    fun resetLiveData(page: Int) {
        _pld.value = PageScrollModel(
            page = page,
            hideDialog = false,
            scroll = false
        )
    }


}