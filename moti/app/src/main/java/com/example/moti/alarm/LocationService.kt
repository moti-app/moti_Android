package com.example.moti.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.moti.R
import com.example.moti.ui.main.MainActivity

class LocationService : Service(), LocationListener {
    private val notificationManager
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        registerDefaultNotificationChannel()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_LOCATION_ID, createLocationNotification())
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //위치 갱신 최소 시간, 위치 갱신 최소 거리
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.01f, this)
        } else {
            Log.e(TAG, "Location permissions are not granted.")
        }
    }

    private fun createLocationNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Location Service")
            setContentText("Tracking location...")
            setSmallIcon(R.drawable.ic_launcher_background)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(pendingIntent)
        }.build()
    }

    private fun registerDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createDefaultNotificationChannel())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDefaultNotificationChannel() =
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
            description = CHANNEL_DESCRIPTION
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

    override fun onLocationChanged(location: Location) {
        //위치가 변경된 경우 - n초 간격마다 수행되는 함수
        Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")

        //AlarmShooter을 사용해서 알림을 보낼지 말지 정하고 배너 알림을 보낸다.
        val alarmShooter = AlarmShooter(this)
        alarmShooter.checkLocation(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_LOCATION_ID = 1
        private const val CHANNEL_ID = "location_channel"
        private const val CHANNEL_NAME = "Location Service"
        private const val CHANNEL_DESCRIPTION = "This channel is used by Location Service"
    }
}