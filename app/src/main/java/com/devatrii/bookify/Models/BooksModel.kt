package com.devatrii.bookify.Models

import java.io.Serializable

data class BooksModel(
    val id: String = "",
    val image: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
//    val category: String = "",
    val position: Int = -1,
    val bookPDF: String = "",
): Serializable