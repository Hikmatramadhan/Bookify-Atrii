package com.devatrii.bookify.ViewModels

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScreenshotViewModel() : ViewModel() {

    suspend fun takeScreenshot(activity: Activity, fileName: String) {
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val screenshot = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false
        saveScreenshot(screenshot, fileName, activity)
    }

    suspend fun saveScreenshot(bitmap: Bitmap, fileName: String, activity: Activity) {
        val screenshotsDir = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${File.separator}screenshots"
        )

        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs()
        }

        val screenshotFile = File(screenshotsDir, "$fileName.png")
        try {
            withContext(Dispatchers.IO) {
                FileOutputStream(screenshotFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            shareScreenshot(activity, screenshotFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareScreenshot(activity: Activity, screenshotFile: File?) {
        if (screenshotFile != null && screenshotFile.exists()) {
            try {
                val screenshotUri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    screenshotFile
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Add this line to grant read permission to the receiving app
                activity.startActivity(Intent.createChooser(intent, "Share Screenshot"))
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exception if necessary
            }
        }
    }



}