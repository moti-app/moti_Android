package com.example.moti.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.moti.R
import com.example.moti.data.Alarmtone
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Week
import com.example.moti.data.ringtoneManagerUri
import com.example.moti.ui.alarm.FullScreenAlarmActivity
import com.example.moti.ui.main.MainActivity
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
                    Log.e("SLEEP", alarm.isSleep.toString())
                    if(alarm.isSleep){
                        continue
                    }
                    val alarmLocation = Location("").apply {
                        latitude = alarm.location.x
                        longitude = alarm.location.y
                    }

                    val distance = currentLocation.distanceTo(alarmLocation)
                    val now = LocalDateTime.now()
                    val today = Week.values()[now.dayOfWeek.ordinal]

                    Log.e("REPEAT", alarm.isRepeat.toString())
                    if(!alarm.isRepeat){
                        //반복 기능이 없다면 앞으로 울리지 않는다!
                        alarm.isSleep = true
                    }
                    else if (alarm.repeatDay != null && !alarm.repeatDay!!.contains(today)) {
                        //반복인데 repeatDay에 오늘이 포함되어 있지 않다면 패스!!
                        continue
                    }
                    if (distance <= alarm.radius && alarm.whenArrival || distance >= alarm.radius && !alarm.whenArrival) {
                        val now = LocalDateTime.now()
                        val lastNoti = alarm.lastNoti
                        val intervalMinutes = alarm.interval ?: 1440

                        if (lastNoti == null || ChronoUnit.MINUTES.between(lastNoti, now) >= intervalMinutes) {

                            if(alarm.hasBanner){
                                //배너 설정 on -> 배너 알림
                                sendNotification(alarm)
                            }
                            else{
                                //전체화면알림
                                sendFullScreenAlarm(context, alarm)
                            }

                            alarm.lastNoti = now
                            database?.alarmDao()?.update(alarm)
                        }
                    }

                }
            }
        }
    }

    private fun sendFullScreenAlarm(context: Context, alarm: Alarm) {
        Log.e("aa", "sendFullScreenAlarm22")
        acquireWakeLock(context)
        // 기본 알림 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.high_importance_channel_name)
            val descriptionText = context.getString(R.string.high_importance_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                context.getString(R.string.high_noti_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        // 전체 화면 알림을 위한 Activity Intent 생성
        val fullscreenIntent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            action = "fullscreen_activity"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_ID", 218) // 알림 ID 전달
            putExtra("AlarmID", alarm.alarmId)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, fullscreenIntent, PendingIntent.FLAG_IMMUTABLE)
        val fullscreenPendingIntent = PendingIntent.getActivity(context, 0, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, context.getString(R.string.high_noti_channel_id)).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("fullscreen intent notification")
            setContentText("fullscreen intent notification!")
            setAutoCancel(true)
            setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLocalOnly(true)
            priority = NotificationCompat.PRIORITY_MAX
            setContentIntent(pendingIntent)
            // 전체 화면 인텐트 설정
            setFullScreenIntent(fullscreenPendingIntent, true)
        }

        // 알림을 트리거
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(218, builder.build())

        // 잠금화면에서도 알림이 뜨도록 인텐트를 시작
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startActivity(fullscreenIntent)
        }

        startAlarmService(context, alarm)
    }

    private fun startAlarmService(context: Context, alarm: Alarm) {
        var intent = Intent();
        if (alarm.alarmtone?.ringtoneManagerUri() == null){
            //무음일 경우
            var defaultAlarmTone = Alarmtone.SystemDefault;
            intent = Intent(context, FullScreenAlarmService::class.java).apply {
                action = FullScreenAlarmService.ACTION_ALARM_ON
                putExtra(FullScreenAlarmService.EXTRA_ALARM_URI, defaultAlarmTone.ringtoneManagerUri())
                putExtra(FullScreenAlarmService.EXTRA_ALARM_VOLUME, 0) //무음이기 때문에 0
                putExtra(FullScreenAlarmService.EXTRA_ALARM_VIBRATE, alarm.useVibration)
            }
        }
        else{
            //소리가 있는 경우
            intent = Intent(context, FullScreenAlarmService::class.java).apply {
                action = FullScreenAlarmService.ACTION_ALARM_ON
                putExtra(FullScreenAlarmService.EXTRA_ALARM_URI, alarm.alarmtone!!.ringtoneManagerUri())
                putExtra(FullScreenAlarmService.EXTRA_ALARM_VOLUME, 100) // 필요시 알람 볼륨 설정
                putExtra(FullScreenAlarmService.EXTRA_ALARM_VIBRATE, alarm.useVibration)
            }
        }

        context.startService(intent)
        Log.e("aa", "full startAlarmService")
    }

    fun acquireWakeLock(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::MyWakelockTag"
        )
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
    }

    private fun sendFullScreenAlarmFAIL(alarm: Alarm) {
        //실패작 함수
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

        notificationManager.notify(alarm.alarmId.toInt() + 1024, notification)
    }
}
