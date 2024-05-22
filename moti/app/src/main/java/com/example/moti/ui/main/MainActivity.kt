package com.example.moti.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.ActivityMainBinding
import com.example.moti.ui.map.MapFragment
import com.example.moti.ui.memo.MemoFragment
import com.example.moti.R
import com.example.moti.alarm.LocationService
import com.example.moti.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val radiusViewModel: RadiusViewModel by viewModels()
    private val radioButtonViewModel: RadioButtonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        initBottomNavigation()

        setContentView(binding.root)
        scheduleLocationCheck()
        checkPermissions()
    }
    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 132)
        }
    }
    override fun onResume() {
        super.onResume()
        //scheduleLocationCheck()
    }
    private fun scheduleLocationCheck() {
        //포어그라운드 서비스 시작!
        Intent(this, LocationService::class.java).run {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(this)
            else startService(this)
        }
    }
    private fun stopForegroundService() {
        Intent(this, LocationService::class.java).run {
            stopService(this)
        }
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

                R.id.lookFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MemoFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.searchFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MemoFragment()) // 추후 변경
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.lockerFragment -> {
                    supportFragmentManager.beginTransaction()
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }



    }
}
