package com.example.moti.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.moti.R
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.ActivityMainBinding
import com.example.moti.ui.map.MapFragment
import com.example.moti.ui.memo.MemoFragment



class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val radiusViewModel: RadiusViewModel by viewModels()
    private val radioButtonViewModel: RadioButtonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        initBottomNavigation()

        setContentView(binding.root)
    }

    private fun initBottomNavigation() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, MapFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener{ item ->

            when (item.itemId) {

                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MapFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.memoFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MemoFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

            }
            false
        }



    }
}