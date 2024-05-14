package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.moti.R
import com.example.moti.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var searchFragment: SearchFragment? = null
    private var timer: CountDownTimer? = null
    private val delayInMillis: Long = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment,SearchDefaultFragment())
        fragmentTransaction.commit()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchSv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    //searchFragment?.searchPlaces(query)
                    Log.d("submit","$query")
                    return true
                }
                return false
            }

            @SuppressLint("ResourceType")
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    if (searchFragment==null) {
                        loadFragmentWithQuery(newText)
                    }
                    timer?.cancel() // 이전 타이머가 있다면 취소
                    timer = object : CountDownTimer(delayInMillis, delayInMillis) {
                        override fun onTick(millisUntilFinished: Long) {
                            // 아무것도 하지 않음
                        }

                        override fun onFinish() {
                            // 딜레이가 끝나면 API 요청을 보냄
                            newText.let {
                                if (it.isNotEmpty()) {
                                    loadFragmentWithQuery(newText)
                                }
                            }
                        }
                    }.start()
                    return true
                }
                else {
                    timer?.cancel()
                    searchFragment=null
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment,SearchDefaultFragment())
                    fragmentTransaction.commit()
                    return false
                }
            }

        })
    }
    @SuppressLint("ResourceType")
    private fun loadFragmentWithQuery(query: String?) {
        if (searchFragment == null) {
            searchFragment = SearchFragment.newInstance(query!!)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, searchFragment!!)
                .commit()
        } else {
            searchFragment?.updateQuery(query)
        }
    }
}

