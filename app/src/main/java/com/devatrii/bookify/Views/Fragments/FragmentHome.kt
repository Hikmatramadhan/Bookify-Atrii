package com.devatrii.bookify.Views.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.devatrii.bookify.Adapters.HomeAdapter
import com.devatrii.bookify.Models.HomeModel
import com.devatrii.bookify.R
import com.devatrii.bookify.Repository.MainRepo
import com.devatrii.bookify.Utils.FirebaseResponse
import com.devatrii.bookify.Utils.removeViewAnim
import com.devatrii.bookify.Utils.showViewAnim
import com.devatrii.bookify.ViewModels.MainViewModel
import com.devatrii.bookify.ViewModels.MainViewModelFactory
import com.devatrii.bookify.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    lateinit var activity: AppCompatActivity
    var list: ArrayList<HomeModel> = ArrayList()
    lateinit var adapter: HomeAdapter


    lateinit var mainRepo: MainRepo
    lateinit var viewModel: MainViewModel
    private val TAG = "MainActivity"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = getActivity() as AppCompatActivity
        adapter = HomeAdapter(list, activity)
        mainRepo = MainRepo(activity)
        viewModel =
            ViewModelProvider(activity, MainViewModelFactory(mainRepo))[MainViewModel::class.java]

        binding.apply {
            mRecyclerViewHome.adapter = adapter
            mRecyclerViewHome.onScroll()
            handleBackend()

            mError.mTryAgain.setOnClickListener {
                viewModel.getHomeData()
            }
        }


        return binding.root
    }

    private fun handleBackend() {
        viewModel.getHomeData()
        viewModel.homeLiveData.observe(activity) {
            Log.d(TAG, "handleBackend: $it")
            when (it) {
                is FirebaseResponse.Error -> {
                    // handle error
                    binding.mLoader.removeViewAnim()
                    binding.mErrorHolder.showViewAnim()
                }

                is FirebaseResponse.Loading -> {
                    // handle loading
                    binding.mLoader.showViewAnim()
                    binding.mErrorHolder.removeViewAnim()
                }

                is FirebaseResponse.Success -> {
                    // handle success
                    binding.mLoader.removeViewAnim()
                    binding.mErrorHolder.removeViewAnim()
                    list.clear()
                    it.data?.forEach {
                        list.add(it)
                        adapter.notifyItemChanged(list.size)
                    }
                }
            }
        }
    }

    private fun RecyclerView.onScroll() {
        val state = IntArray(1)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                state[0] = newState
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && (state[0] == 0 || state[0] == 2)) {
                    (getActivity() as AppCompatActivity).supportActionBar?.hide()
                } else if (dy < -10) {
                    (getActivity() as AppCompatActivity).supportActionBar?.show()
                }
            }
        })
    }

}