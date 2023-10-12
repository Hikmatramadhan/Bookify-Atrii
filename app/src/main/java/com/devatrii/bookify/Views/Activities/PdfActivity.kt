package com.devatrii.bookify.Views.Activities


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.devatrii.bookify.Adapters.NotesAdapter
import com.devatrii.bookify.AppDb.AppDatabase
import com.devatrii.bookify.AppDb.Entities.BookmarkEntity
import com.devatrii.bookify.AppDb.Entities.NotesEntity
import com.devatrii.bookify.R
import com.devatrii.bookify.Utils.CustomScrollHandler
import com.devatrii.bookify.Utils.SharedPrefUtils
import com.devatrii.bookify.Utils.SpringScrollHelper
import com.devatrii.bookify.Utils.bitmapToUri
import com.devatrii.bookify.Utils.checkPermission
import com.devatrii.bookify.Utils.hideStatusBar
import com.devatrii.bookify.Utils.hideView
import com.devatrii.bookify.Utils.removeView
import com.devatrii.bookify.Utils.sharePdfWithFileProvider
import com.devatrii.bookify.Utils.showMessage
import com.devatrii.bookify.Utils.showStatusBar
import com.devatrii.bookify.Utils.showView
import com.devatrii.bookify.Utils.springDown
import com.devatrii.bookify.Utils.springUp
import com.devatrii.bookify.ViewModels.PageScrollViewModel
import com.devatrii.bookify.ViewModels.ScreenshotViewModel
import com.devatrii.bookify.Views.Fragments.BookmarksFragment
import com.devatrii.bookify.databinding.ActivityPdfBinding
import com.devatrii.bookify.databinding.LayoutAddNoteBinding
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class PdfActivity : AppCompatActivity() {
    val activity = this
    lateinit var binding: ActivityPdfBinding
    val TAG = "PDF_VIEW"
    lateinit var bookMarkItem: MenuItem
    var isFullScreen: Boolean = false
    val screenshotViewModel by lazy {
        ViewModelProvider(activity)[ScreenshotViewModel::class.java]
    }
    var bookPDF = ""

    private val db by lazy {
        AppDatabase.getDatabase(activity)
    }
    private val bookmarkDao by lazy {
        db.bookmarkDao()
    }
    lateinit var bookId: String
    private var book_db_id = 0
    var currentPage = 0

    val pageScrollViewModel by lazy {
        ViewModelProvider(activity as ViewModelStoreOwner)[PageScrollViewModel::class.java]
    }


    var MAX_PAGES = 0
    var isAddingNote = false

    var isNotes = false

    private val IMAGE_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_REQUEST_CODE = 102
    private val STORAGE_PERMISSION_REQUEST_CODE = 103
    val dialogBinding by lazy {
        LayoutAddNoteBinding.inflate(layoutInflater)
    }
    var noteImagePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
//        supportActionBar?.hide()

        binding.apply {
            SharedPrefUtils.init(activity)
            bookPDF = intent.getStringExtra("book_pdf").toString()
//            bookPDF =
//                "file:///storage/emulated/0/Android/data/com.devatrii.bookify/files/Download/ViCrEjxo0XJZawDMKPPr/StealLikeAnArtist.pdf/Firebase%20Introduction.pdf"
            bookId = bookPDF.split("Download/").joinToString("/")
            // Disabled Log Log.i(TAG, "onCreate: $bookPDF")



            pdfView.fromUri(Uri.parse(bookPDF)).swipeHorizontal(false)
                .scrollHandle(CustomScrollHandler(activity)).onLoad {
                    // Disabled Log Log.i(TAG, "onLoad: All Pages Drawn")
                    updateBookmarkUI()
                    openLastRead()
                    MAX_PAGES = it
                }.onPageChange(object : OnPageChangeListener {
                    override fun onPageChanged(page: Int, pageCount: Int) {
                        if (page > 1) saveLastRead(page)
                        currentPage = page
                        updateBookmarkUI()
                        current_notes_page = currentPage
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                if (isNotes) {
                                    if (!isAddingNote) notesLogic()

                                    try {
                                        if (page > 1) updateNotesPage("On Scroll")
                                        else updateNotePageText()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                    }

                }).enableSwipe(true).enableDoubletap(true).enableAntialiasing(true).pageSnap(true)
                .autoSpacing(true).pageFling(true).load()
//        pdfView.setBackgroundColor(R.color.colorPrimary)


            pageScrollViewModel.pageNumberLd.observe(activity) {
                if (it.scroll) pdfView.jumpTo(it.page, true)
            }
            notesHolder()

        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun notesHolder() {
        binding.apply {
            noteSettings()
            notePopupActions()
            mNotesHolder.setOnTouchListener { view, event ->

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }

                    MotionEvent.ACTION_MOVE -> {
                        view.y = event.rawY + dY
                        view.x = event.rawX + dX
                    }

                    else -> false
                }


                true
            }
        }
    }

    private var dX = 0f
    private var dY = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun notesLogic() {
        isNotes = true
        binding.apply {
            getAllNotes(calledFrom = "Notes Logic")
            notePagination()
        }
    }

    var current_notes_page = 0
    private fun notePagination() {
        binding.mNotes.apply {
            mPreviousPage.hideView()
            mNextPage.hideView()
            mNextPage.setOnClickListener {
                if (current_notes_page < MAX_PAGES) {
                    current_notes_page++
                    updateNotesPage("Next Page Btn")
                } else showSnackBar("Last Page")

            }
            mPreviousPage.setOnClickListener {
                if (current_notes_page > 0) {
                    current_notes_page--
                    updateNotesPage("Previous Page Btn")
                } else showSnackBar("First Page")
            }

        }
    }

    private fun updateNotesPage(calledFrom: String) {
        getAllNotes(current_notes_page, calledFrom)
        binding.mNotes.apply {
            mPageIndex.text = "Page ${current_notes_page + 1}"
        }
    }

    private fun updateNotePageText() {
        binding.mNotes.apply {
            mPageIndex.text = "Page ${current_notes_page + 1}"
        }
    }

    val list = ArrayList<NotesEntity>()
    private fun getAllNotes(pageNo: Int = currentPage, calledFrom: String) {
        val notesAdapter = NotesAdapter(list, activity)
        binding.mNotes.mNotesRv.apply {
            adapter = notesAdapter
            SpringScrollHelper().attachToRecyclerView(this)
        }
        CoroutineScope(Dispatchers.IO).launch {
            val noteList = db.notesDao().getBookmarkByPage(bookId, pageNo)
            withContext(Dispatchers.Main) {
                noteList?.let {
                    val tempOldList = ArrayList<NotesEntity>()
                    list.forEach { note ->
                        tempOldList.add(note)
                    }
                    updateNotesAdapter(oldList =tempOldList, newList = it, notesAdapter = notesAdapter)
                }
            }
        }
    }

    private fun updateNotesAdapter(
        oldList: ArrayList<NotesEntity>, newList: List<NotesEntity>, notesAdapter: NotesAdapter
    ) {
        val tempNewList = ArrayList<NotesEntity>()
        newList.forEach {
            tempNewList.add(it)
        }
        notesAdapter.submitList(oldList, tempNewList)
        list.clear()
        newList.forEach {
            list.add(it)
        }
        if (newList.isNotEmpty()) {
            binding.mNotes.mNotFound.removeView()
            binding.mNotes.mNotesRv.showView()
        } else {
            binding.mNotes.mNotFound.showView()
            binding.mNotes.mNotesRv.removeView()
        }
    }

    private fun notePopupActions() {
        binding.apply {
            mNotes.mAddNote.setOnClickListener {
                addNote()
            }
            mNotes.mHideNotes.setOnClickListener {
                isNotes = false
                mNotesHolder.springDown()
                mNotesAdjusterHolder.springDown(fv = mNotesAdjusterHolder.height.toFloat())
            }
            mNotes.mNotesSettings.setOnClickListener {
                if (mNotesAdjusterHolder.visibility == View.GONE) {
                    mNotesAdjusterHolder.springUp()
                    mNotes.mNotesSettings.setImageDrawable(resources.getDrawable(R.drawable.ic_tools_setting_opened))
                } else {
                    mNotesAdjusterHolder.springDown(fv = mNotesAdjusterHolder.height.toFloat())
                    mNotes.mNotesSettings.setImageDrawable(resources.getDrawable(R.drawable.ic_tools_settings))
                }
            }
            mNotes.mRefresh.setOnClickListener {
                getAllNotes(calledFrom = "Notes Holder")
            }
        }

    }

    private fun noteSettings() {

        binding.apply {

            mNotesAdjuster.apply {
                mNotesHolder.scaleX = 60f / 100
                mNotesHolder.scaleY = 60f / 100
                mSize.addOnChangeListener(object : Slider.OnChangeListener {
                    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                        if (fromUser) {
                            // Disabled Log Log.i(
//                                TAG,
//                                "onValueChange: Value $value => Note Size X axis => ${mNotesHolder.scaleX}, Size Y axis => ${mNotesHolder.scaleY}"
//                            )
                            mNotesHolder.scaleX = (value / 100)
                            mNotesHolder.scaleY = value / 100

                        }
                    }
                })

            }
        }
    }

    private fun addNote() {
        checkAndRequestPermissions()
        isAddingNote = true
        val dialog = Dialog(activity).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (dialogBinding.root.parent != null) {
                (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root) // <- fix
            }
            setContentView(dialogBinding.root)
            setCancelable(false)
            show()
            val window: Window = this.window!!
            window.setLayout(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
            )
        }
        val updateText: (newText: String) -> Unit = { newText ->
            dialogBinding.mSampleNote.apply {
                if (newText.isEmpty()) dialogBinding.mAddNoteBtn.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_tools_cancel
                    )
                )
                else dialogBinding.mAddNoteBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_tools_send))
                mNote.text = newText
            }
        }
        dialogBinding.apply {
            mSampleNote.mNote.maxLines = Int.MAX_VALUE

            mNoteED.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    updateText(p0.toString())
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })

            mCameraBtn.setOnClickListener {
                // select image from gallery
                selectOrCaptureImage()
            }

            mAddNoteBtn.setOnClickListener {
                if (mNoteED.text.toString().isEmpty()) {
                    dialog.dismiss()
                    return@setOnClickListener
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val model = NotesEntity(
                        imagePath = noteImagePath,
                        note = mNoteED.text.toString(),
                        pageNo = currentPage,
                        book_id = bookId
                    )
                    // Disabled Log Log.i(TAG, "addNote: $model")
                    db.notesDao().insertNote(model)
                    noteImagePath = ""
                    // resting data
                    isAddingNote = false
                    withContext(Dispatchers.Main) {
                        updateNotesPage("Add Note")
                    }
                }
                dialog.dismiss()
                mNoteED.text = null
                mSampleNote.mNote.text = null
                mSampleNote.mNoteImage.setImageBitmap(null)
                showSnackBar("Note Saved")
            }

        }
    }

    private fun selectOrCaptureImage() {
        if (!activity.checkPermission(Manifest.permission.CAMERA)) {
            // please enable permission from setting
            showMessage("Please enable permissions", activity)
            return
        }
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val chooserIntent = Intent.createChooser(intent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, rData: Intent?) {
        super.onActivityResult(requestCode, resultCode, rData)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val data = rData

            var imageUri: Uri? = null

            if (data != null) {
                if (data.hasExtra("data")) {
                    // The image was captured using the camera, and a thumbnail is provided in the extras
                    val thumbnailBitmap = data.extras?.get("data") as? Bitmap
                    if (thumbnailBitmap != null) {
                        imageUri = bitmapToUri(activity, thumbnailBitmap)
                    }
                } else if (data.data != null) {
                    // The image was selected from the gallery
                    imageUri = data.data
                }
            }

            imageUri?.let {
                // Copy the selected/captured image to the app's internal storage
                val copiedUri = copyImageToInternalStorage(imageUri)
                copiedUri?.let {
                    dialogBinding.mSampleNote.mNoteImage.setImageURI(it)
                    dialogBinding.mSampleNote.mNoteImage.showView()
                    noteImagePath = it.toString()
                }
                // Do something with the copiedUri
            }
        }

    }

    private fun copyImageToInternalStorage(sourceUri: Uri): Uri? {
        val sourceStream = contentResolver.openInputStream(sourceUri)
        val fileName = "image_${System.currentTimeMillis()}.jpg"

        val destinationDirectory =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/notes")
        destinationDirectory.mkdirs() // Create the directory if it doesn't exist

        val destinationFile = File(destinationDirectory, fileName)
        val destinationStream = FileOutputStream(destinationFile)

        sourceStream?.use { input ->
            destinationStream.use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(destinationFile)
    }


    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
            } else {
                // Camera permission denied
            }
        }

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Storage permission granted
            } else {
                // Storage permission denied
            }
        }
    }


    private fun openLastRead() {
        val PREF_SHOW_LAST_READ_KEY = "PREF_SHOW_LAST_READ"
        SharedPrefUtils.apply {
            val pageNo = getPrefInt(bookId, 0)
            if (pageNo > 0) {
                val isShowDialog = getPrefBoolean(PREF_SHOW_LAST_READ_KEY, true)
                if (isShowDialog) {
                    MaterialAlertDialogBuilder(activity).apply {
                        setTitle("Last Read Found!")
                        setMessage("Last time you were at ${pageNo + 1}. Do you want to jump to page no ${pageNo + 1}")
                        setPositiveButton("Jump") { _, _ ->
                            binding.pdfView.jumpTo(pageNo, true)
                        }
                        setNegativeButton("Cancel", null)
                        setNeutralButton("Don't Show Again") { _, _ ->
                            putPrefBoolean(PREF_SHOW_LAST_READ_KEY, false)
                            showSnackBar("You won't be able to jump now!", "Undo", buttonAction = {
                                putPrefBoolean(PREF_SHOW_LAST_READ_KEY, true)
                                showSnackBar("Done")
                            })
                        }
                        show()
                    }
                }
            }

        }
    }

    private fun saveLastRead(pageNo: Int) {
        SharedPrefUtils.putPrefInt(bookId, pageNo)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        with(menuInflater) {
            inflate(R.menu.top_menu, menu)
            if (menu is MenuBuilder) {
                menu.setOptionalIconsVisible(true)
                for (item in menu.visibleItems) {
                    item.icon?.apply {
                        setTint(resources.getColor(R.color.colorPrimary))
                    }
                    // assigning items
                    if (item.itemId == R.id.option_bookmarks) {
                        bookMarkItem = item
                    }

                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_bookmarks -> {
                item.isChecked = !item.isChecked
                val imageDrawable =
                    if (item.isChecked) R.drawable.ic_menu_bookmark_remove else R.drawable.ic_menu_bookmark_add
                item.apply {
                    var bookmarkText = if (isChecked) "Bookmarked" else "Bookmark"
                    setIcon(imageDrawable)
                    icon?.setTint(resources.getColor(R.color.colorPrimary))
                    title = bookmarkText
                    bookmarkText = if (isChecked) "Bookmarked" else "Bookmark Removed"
                    if (isChecked) {
                        // add bookmark
                        CoroutineScope(Dispatchers.IO).launch {
                            bookmarkDao.insertBookMark(
                                BookmarkEntity(
                                    book_id = bookId, pageNo = currentPage
                                )
                            )
                        }

                    } else {
                        // remove bookmark
                        CoroutineScope(Dispatchers.IO).launch {
                            bookmarkDao.deleteBookmark(
                                BookmarkEntity(
                                    id = book_db_id, book_id = bookId, pageNo = currentPage
                                )
                            )
                        }
                    }

                    showSnackBar(bookmarkText)
                }
            }

            R.id.option_add_notes -> {
                isNotes = true
                binding.mNotesHolder.springUp()
                updateNotesPage("On Scroll")
            }

            R.id.option_show_bookmarks -> {
                pageScrollViewModel.resetLiveData(currentPage)
                val bottomSheetFragment = BookmarksFragment()
                val bundle = Bundle()
                bundle.putString("book_id", bookId)
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            }

            R.id.option_full_screen -> {
                hideStatusBar()
                isFullScreen = true
            }

            R.id.option_share_screenshot -> {
                CoroutineScope(Dispatchers.IO).launch {
                    screenshotViewModel.takeScreenshot(
                        activity, "Screenshot-${System.currentTimeMillis()}"
                    )
                }
            }

            R.id.option_open_pdf -> {
                Uri.parse(bookPDF).path?.let {
                    sharePdfWithFileProvider(activity, File(it))
                }
            }

            R.id.option_night_mode -> {
                item.isChecked = !item.isChecked
                binding.pdfView.setNightMode(item.isChecked)
                val status = if (item.isChecked) "Enabled" else "Disabled"
                showSnackBar("Night Mode $status")
                if (item.isChecked) {
                    binding.pdfView.apply {
                        if (currentPage > 1) {
                            jumpTo(currentPage - 1)
                            jumpTo(currentPage + 1)
                        } else {
                            jumpTo(currentPage + 1)
                            jumpTo(currentPage - 1)

                        }
                    }

                }

                val imageDrawable =
                    if (item.isChecked) R.drawable.ic_menu_light_mode else R.drawable.ic_menu_night_mode
                item.apply {
                    val bookmarkText = if (isChecked) "Light Mode" else "Night Mode"
                    setIcon(imageDrawable)
                    icon?.setTint(resources.getColor(R.color.colorPrimary))
                    title = bookmarkText
                }


            }

            else -> {
                // other
            }
        }
        return true
    }

    private fun updateBookmarkUI() {
        if (checkIfBookmarked()) {
            changeBookmarkState(true)
        } else {
            changeBookmarkState()
        }
    }

    private fun checkIfBookmarked(): Boolean {
        var isBookmarked = false // Default value is false

        runBlocking {
            val result: BookmarkEntity? = async {
                bookmarkDao.getBookmark(bookId, currentPage).apply {
                    this?.let {
                        book_db_id = it.id
                    }
                }
            }.await()

            // Disabled Log Log.i(TAG, "checkIfBookmarked: Result Coroutine $result")

            // Update isBookmarked based on the result
            isBookmarked = result != null
        }

        // Disabled Log Log.i(TAG, "checkIfBookmarked: Result $isBookmarked Book Id: $book_db_id")

        return isBookmarked
    }

    private fun changeBookmarkState(isBookmarked: Boolean = false) {
        bookMarkItem.apply {
            isChecked = isBookmarked
            val imageDrawable =
                if (isChecked) R.drawable.ic_menu_bookmark_remove else R.drawable.ic_menu_bookmark_add
            setIcon(imageDrawable)
            icon?.setTint(resources.getColor(R.color.colorPrimary))
            val bookmarkText = if (isChecked) "Bookmarked" else "Bookmark"
            title = bookmarkText
        }
    }

    private fun showSnackBar(
        message: String, buttonText: String = "", buttonAction: (() -> Unit) = {}
    ) {
        val snackbar = Snackbar.make(
            binding.mCoordinatorLayout, message, Snackbar.LENGTH_LONG
        )
        if (buttonText.isNotEmpty()) {
            snackbar.setAction(buttonText) {
                buttonAction()
            }
        }
        snackbar.show()
    }

    override fun onBackPressed() {
        if (isFullScreen) {
            showStatusBar()
            isFullScreen = false
            return
        }
        super.onBackPressed()
    }


}