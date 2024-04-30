package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.moti.R
import com.example.moti.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var searchFragment: SearchFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment,SearchDefaultFragment())
        fragmentTransaction.commit()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchSv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            @SuppressLint("ResourceType")
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    loadFragmentWithQuery(newText)
                    return true
                }
                else {
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

