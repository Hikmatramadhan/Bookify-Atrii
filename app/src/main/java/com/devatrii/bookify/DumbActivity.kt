package com.devatrii.bookify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devatrii.bookify.Views.Fragments.SearchFragment
import com.devatrii.bookify.databinding.ActivityDumbBinding

class DumbActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDumbBinding.inflate(layoutInflater)
    }
    private val activity = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

        }
    }
}