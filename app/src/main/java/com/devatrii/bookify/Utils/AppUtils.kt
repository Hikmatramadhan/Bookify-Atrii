package com.devatrii.bookify.Utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun generateSubstrings(name: String): ArrayList<String> {
    val substrings = ArrayList<String>()
    var currentSubstring = ""
    for (char in name) {
        currentSubstring += char
        substrings.add(currentSubstring.lowercase())
    }
    val tempSplittedName = name.split(" ")
    val splittedName = ArrayList<String>()
    tempSplittedName.forEach {
        splittedName.add(it)
    }
    splittedName.removeAt(0)
    for (n in splittedName) {
        currentSubstring = ""
        for (char in n) {
            currentSubstring += char
            substrings.add(currentSubstring.lowercase())
        }
    }
    return substrings
}

fun showMessage(message: String = "Message", context: Context, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length).show()
}


fun formatFileSize(bytes: Long): String {
    val kiloBytes = bytes / 1024.0
    if (kiloBytes < 1) {
        return "$bytes B"
    }

    val megaBytes = kiloBytes / 1024.0
    return if (megaBytes < 1) {
        String.format("%.2f kB", kiloBytes)
    } else {
        String.format("%.2f mB", megaBytes)
    }
}

fun Activity.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    // Save the bitmap to a temporary file
    val cachePath = File(context.externalCacheDir, "tempImage.png")
    try {
        val stream = FileOutputStream(cachePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    // Get the content resolver
    val contentResolver: ContentResolver = context.contentResolver

    // Get the saved image's uri
    val imagePath = cachePath.absolutePath
    return try {
        Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, imagePath, "Title", null))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}