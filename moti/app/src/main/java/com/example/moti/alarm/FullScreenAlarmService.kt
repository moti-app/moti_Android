package com.example.moti.alarm

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

class FullScreenAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var audioManager: AudioManager
    private var savedVolume: Int = 0

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_ALARM_ON -> {
                    val alarmUri = Uri.parse(intent.getStringExtra(EXTRA_ALARM_URI))
                    val volume = intent.getIntExtra(EXTRA_ALARM_VOLUME, 100)
                    val vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
                    startAlarm(alarmUri, volume, vibrate)
                }
                ACTION_ALARM_OFF -> stopAlarm()
            }
        }
        return START_STICKY
    }

    private fun startAlarm(alarmUri: Uri, volume: Int, vibrate: Boolean) {
        Log.e("aa", "startAlarmService")
        stopAlarm()

        savedVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val setVolume = (maxVolume * (volume / 100.0)).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, setVolume, 0)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, alarmUri)
            setAudioStreamType(AudioManager.STREAM_ALARM)
            isLooping = true
            prepare()
            start()
        }

        if (vibrate) {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
            } else {
                vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, savedVolume, 0)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    companion object {
        const val ACTION_ALARM_ON = "com.example.moti.alarm.ACTION_ALARM_ON"
        const val ACTION_ALARM_OFF = "com.example.moti.alarm.ACTION_ALARM_OFF"
        const val EXTRA_ALARM_URI = "com.example.moti.alarm.EXTRA_ALARM_URI"
        const val EXTRA_ALARM_VOLUME = "com.example.moti.alarm.EXTRA_ALARM_VOLUME"
        const val EXTRA_ALARM_VIBRATE = "com.example.moti.alarm.EXTRA_ALARM_VIBRATE"
        const val EXTRA_NOTIFICATION_ID: String = "com.example.moti.alarm.EXTRA_NOTIFICATION_ID"
    }
}
