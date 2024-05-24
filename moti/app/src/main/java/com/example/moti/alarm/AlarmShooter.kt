package com.example.moti.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.ui.alarm.FullScreenAlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AlarmShooter(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkLocation(currentLocation: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = MotiDatabase.getInstance(context)
            val allAlarms = database?.alarmDao()?.findAllAlarms()

            if (allAlarms != null) {
                for (alarm in allAlarms) {
                    // 반복 구분 및 요일 확인 로직 추가 가능
                    val alarmLocation = Location("").apply {
                        latitude = alarm.location.x
                        longitude = alarm.location.y
                    }

                    val distance = currentLocation.distanceTo(alarmLocation)
                    // 벗어날 때 구분 로직 추가 가능
                    if (distance <= alarm.radius) {
                        val now = LocalDateTime.now()
                        val lastNoti = alarm.lastNoti
                        val intervalMinutes = alarm.interval ?: 1440

                        if (lastNoti == null || ChronoUnit.MINUTES.between(lastNoti, now) >= intervalMinutes) {
                            sendFullScreenAlarm(alarm)
                            alarm.lastNoti = now
                            database?.alarmDao()?.update(alarm)
                        }
                    }
                }
            }
        }
    }
    private fun sendFullScreenAlarm(alarm: Alarm) {
        Log.e("aa", "sendFullScreenAlarm")
        val intent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_TITLE", alarm.title)
            putExtra("ALARM_CONTEXT", alarm.context)
            putExtra("ALARM_ID", alarm.alarmId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarm.alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "fullscreen_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FullScreen Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for full screen alarm notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(alarm.title)
            .setContentText(alarm.context)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarm.alarmId.toInt(), notification)
        context.startActivity(intent)
    }
    private fun sendNotification(alarm: Alarm) {
        // 알림을 보냅니다.
        // 전체 화면 알림 구분 로직 추가 가능
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_ID = "100"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(alarm.title)
            .setContentText(alarm.context)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarm.alarmId.toInt(), notification)
    }
}
