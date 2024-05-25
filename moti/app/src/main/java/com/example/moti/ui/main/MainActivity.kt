package com.example.moti.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.moti.R
import com.example.moti.alarm.LocationService
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Location
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.ActivityMainBinding
import com.example.moti.ui.map.MapFragment
import com.example.moti.ui.memo.MemoFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val radiusViewModel: RadiusViewModel by viewModels()
    private val radioButtonViewModel: RadioButtonViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 321
        private const val LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE = 3211
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 322
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()

        // 권한 요청
        checkPermissions()
        val data: Uri? = intent.data
        if (data != null) {
            val name: String = data.getQueryParameter("param1") ?:""
            val context: String = data.getQueryParameter("param2") ?:""
            val lat: String = data.getQueryParameter("param3") ?:""
            val lng: String = data.getQueryParameter("param4") ?:""
            val radius: String = data.getQueryParameter("param5") ?:""
            if (lat!=""&&lng!="") {
                val db = MotiDatabase.getInstance(this.applicationContext)!!
                val alarmRepository = AlarmRepository(db.alarmDao(),db.tagDao(),db.alarmAndTagDao())

                //Toast.makeText(this, "param1: $param1, param2: $param2", Toast.LENGTH_SHORT).show()
                val alarm = Alarm(
                    title = name,
                    context = context,
                    location = Location(lat.toDouble(),lng.toDouble(),"address",name),
                    whenArrival = true,
                    radius = radius.toDouble(),
                    isRepeat = true,
                    repeatDay = null,
                    hasBanner = true,
                    tagColor = null,
                    lastNoti = LocalDateTime.now().minusDays(1),
                    interval = 1440
                )
                val list: List<Long> = listOf()
                CoroutineScope(Dispatchers.IO).launch {
                    alarmRepository.createAlarmAndTag(alarm, tagIds = list)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "알람이 성공적으로 생성되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        //위치 권한
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        //알림 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            //ACCESS_FINE_LOCATION , ACCESS_COARSE_LOCATION 권한이 모두 허용 되어야 백그라운드 위치 권한 요청 가능
            checkBackgroundLocationPermission()
        }
    }

    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val backgroundLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            if (backgroundLocationPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE
                )
            } else {
                startLocationService()
            }
        } else {
            startLocationService()
        }
    }

    private fun startLocationService() {
        Intent(this, LocationService::class.java).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(this)
            } else {
                startService(this)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // 위치 권한이 부여된 경우 백그라운드 위치 권한 요청
                    checkBackgroundLocationPermission()
                } else {
                    // 권한이 거부된 경우 사용자에게 알림을 표시하거나 다른 조치를 취할 수 있음
                }
            }
            LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 백그라운드 위치 권한이 부여된 경우 위치 서비스 시작
                    startLocationService()
                } else {
                    // 권한이 거부된 경우 사용자에게 알림을 표시하거나 다른 조치를 취할 수 있음
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("이 앱은 배너 알림과 항상 위치 접근 권한이 필요합니다.")
            .setPositiveButton("설정") { _, _ -> checkBackgroundLocationPermission() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun initBottomNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, MapFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener { item ->
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
