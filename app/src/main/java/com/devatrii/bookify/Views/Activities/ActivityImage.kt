package com.devatrii.bookify.Views.Activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.devatrii.bookify.Utils.hideStatusBar
import com.devatrii.bookify.databinding.ActivityImageBinding

class ActivityImage : AppCompatActivity() {
    private val activity = this
    private val binding by lazy {
        ActivityImageBinding.inflate(layoutInflater)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            exitTransition = Fade()
            enterTransition = Fade()

            // shows the transition for 2 seconds
            exitTransition.duration = 2000

        }
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.apply {
            val imageUri = intent.getStringExtra("image_uri")
            imageUri?.let {
                myZoomageView.setImageURI(Uri.parse(it))
            }
        }
    }
}