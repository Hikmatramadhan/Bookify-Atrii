package com.devatrii.bookify.Views.Activities

import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.devatrii.bookify.Adapters.HomeAdapter
import com.devatrii.bookify.Models.HomeModel
import com.devatrii.bookify.R
import com.devatrii.bookify.Repository.BookRepo
import com.devatrii.bookify.Repository.MainRepo
import com.devatrii.bookify.Utils.FirebaseResponse
import com.devatrii.bookify.Utils.removeViewAnim
import com.devatrii.bookify.Utils.showViewAnim
import com.devatrii.bookify.ViewModels.BookViewModel
import com.devatrii.bookify.ViewModels.BookViewModelFactory
import com.devatrii.bookify.ViewModels.MainViewModel
import com.devatrii.bookify.ViewModels.MainViewModelFactory
import com.devatrii.bookify.Views.Fragments.FragmentAccount
import com.devatrii.bookify.Views.Fragments.FragmentHome
import com.devatrii.bookify.Views.Fragments.SearchFragment
import com.devatrii.bookify.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    val activity = this
    var pos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val fragmentHome = FragmentHome()
            val fragmentSearch = SearchFragment()
            val fragmentAccount = FragmentAccount()
            replaceFragment(fragmentHome)
            bottomNavigation.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.mHome -> {
                        if (pos != 0)
                            replaceFragment(fragmentHome, true)
                        pos = 0
                    }

                    R.id.mSearch -> {
                        if (pos != 1)
                            replaceFragment(fragmentSearch, true)
                        pos = 1
                    }

                    R.id.mDownloads -> {
                        if (pos != 2)
                            replaceFragment(fragmentAccount, true)
                        pos = 2
                    }

                    else -> {
                        if (pos != 0)
                            replaceFragment(fragmentHome, true)
                        pos = 0
                    }
                }
                true
            }

        }


    }

    private fun replaceFragment(fragment: Fragment, backStack: Boolean = true) {
        supportFragmentManager.beginTransaction().apply {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            replace(binding.mContainer.id, fragment)
            if (backStack)
                addToBackStack(null)
        }.commit()
    }


}






