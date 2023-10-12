package com.devatrii.bookify.Repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.devatrii.bookify.Adapters.LAYOUT_HOME
import com.devatrii.bookify.Models.BooksModel
import com.devatrii.bookify.Models.HomeModel
import com.devatrii.bookify.Utils.FirebaseResponse
import com.devatrii.bookify.Utils.generateSubstrings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainRepo(context: Context) {
    private val TAG = "MainRepo"
    private val firebaseDatabase = FirebaseFirestore.getInstance()
    private var databaseReference = firebaseDatabase.collection("Home")
    private val homeLD = MutableLiveData<FirebaseResponse<ArrayList<HomeModel>>>()
    val homeLiveData get() = homeLD

    suspend fun getHomeData() {
        homeLiveData.postValue(FirebaseResponse.Loading())

        databaseReference.orderBy(
            "position", com.google.firebase.firestore.Query.Direction.DESCENDING
        ).get().addOnSuccessListener {
            if (it.isEmpty) {
                homeLiveData.postValue(FirebaseResponse.Error("No Books Found"))
                Log.d(TAG, "getHomeData: Data is empty")
                return@addOnSuccessListener
            }
            val tempList = ArrayList<HomeModel>()
            // Assuming 'it' is the documents snapshot returned from Firestore query
            for (document in it) {
                val id = document.getString("id") ?: ""
                val position = document.getLong("position") ?: -1
                val catTitle = document.getString("catTitle") ?: ""
                val LAYOUT_TYPE = document.getLong("LAYOUT_TYPE")?.toInt() ?: LAYOUT_HOME

                val booksList = ArrayList<BooksModel>()
                val booksListSnapshot =
                    document.get("booksList") as? ArrayList<HashMap<String, Any>>
                booksListSnapshot?.let {
                    for (bookData in it) {
                        val id = bookData["id"] as? String ?: ""
                        val position = bookData["position"] as? Int ?: -1
                        val image = bookData["image"] as? String ?: ""
                        val title = bookData["title"] as? String ?: ""
                        val description = bookData["description"] as? String ?: ""
                        val author = bookData["author"] as? String ?: ""
                        val category = bookData["category"] as? String ?: ""
                        val bookPDF = bookData["bookPDF"] as? String ?: ""

                        val booksModel = BooksModel(
                            id = id,
                            image = image,
                            title = title,
                            description = description,
                            author = author,
                            position = position,
                            bookPDF = bookPDF
                        )
                        booksList.add(booksModel)
                    }
                }

                val bodData = document["bod"] as? HashMap<String, Any>
                val bodImage = bodData?.get("image") as? String ?: ""
                val bodTitle = bodData?.get("title") as? String ?: ""
                val bodDescription = bodData?.get("description") as? String ?: ""
                val bodAuthor = bodData?.get("author") as? String ?: ""
                val bodCategory = bodData?.get("category") as? String ?: ""
                val bodBookPDF = bodData?.get("bookPDF") as? String ?: ""
                val bodId = bodData?.get("id") as? String ?: ""
                val bod = BooksModel(
                    id = bodId,
                    image = bodImage,
                    title = bodTitle,
                    description = bodDescription,
                    author = bodAuthor,
                    position = -1,
                    bookPDF = bodBookPDF
                )



                val homeModel =
                    HomeModel(id, catTitle, booksList, bod, LAYOUT_TYPE, position.toInt())
                tempList.add(homeModel)
                Log.d(TAG, "onDataChange: $tempList")
                homeLiveData.postValue(FirebaseResponse.Success(tempList))
            }

        }.addOnFailureListener {
            Log.e(TAG, "onCancelled: $it")
            homeLiveData.postValue(FirebaseResponse.Error("Something Went Wrong With Database"))
        }

    }


    private val bookSearchLD = MutableLiveData<FirebaseResponse<ArrayList<BooksModel>>>()
    val booksLiveData get() = bookSearchLD

    suspend fun searchBook(bookKeywords: ArrayList<String>) {
        val bookDbRef = firebaseDatabase.collection("Books")
            .whereArrayContainsAny("nameSubstrings", bookKeywords)

        bookDbRef.get().addOnSuccessListener {
            if (it.isEmpty) {
                booksLiveData.postValue(FirebaseResponse.Error("No Books Found"))
                Log.d(TAG, "searchBook: No Books Found")
                return@addOnSuccessListener
            }

            val tempList = ArrayList<BooksModel>()
            for (document in it) {
                val book = document.toObject(BooksModel::class.java)
                tempList.add(book)
            }
            Log.d(TAG, "onDataChange: $tempList")
            booksLiveData.postValue(FirebaseResponse.Success(tempList))

        }.addOnFailureListener {
            Log.e(TAG, "onCancelled: $it")
            homeLiveData.postValue(FirebaseResponse.Error("Something Went Wrong With Database"))
        }
    }

    suspend fun addBook(model: BooksModel) {
        // TODO REMOVE THIS FUNCTION IN PRODUCTION
        val bookDbRef = firebaseDatabase.collection("Books")
        val documentId = bookDbRef.document().id

        val data = hashMapOf(
            "id" to documentId,
            "author" to model.author,
            "description" to model.description,
            "image" to model.image,
            "title" to model.title,
            "bookPDF" to model.bookPDF,
            "nameSubstrings" to generateSubstrings(model.title),
        )

        bookDbRef.document(documentId).set(data)
            .addOnSuccessListener {
                // Document is successfully created
                Log.i(TAG, "addBook: Added $it")
            }
            .addOnFailureListener { e ->
                // Handle any errors here
                Log.i(TAG, "addBook: Failed $e")
            }

    }


}





