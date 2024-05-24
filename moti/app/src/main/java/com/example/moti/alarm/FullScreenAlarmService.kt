package com.example.moti.alarm
//
//import android.app.Notification.EXTRA_NOTIFICATION_ID
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.media.AudioAttributes
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.media.RingtoneManager
//import android.net.Uri
//import android.os.Build
//import android.os.IBinder
//import android.os.VibrationEffect
//import android.os.Vibrator
//import android.util.Log
//
//
//class FullScreenAlarmService(private val context: Context) : Service() {
//
//    private var mediaPlayer: MediaPlayer? = null
//    private var vibrator: Vibrator? = null
//
//    lateinit var audioManager: AudioManager
//    private var savedVolume: Int = 0
//    private var savedRingerMode: Int = 0
//
//    override fun onCreate() {
//        super.onCreate()
//        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        if(intent != null) {
//            if(intent.action == ACTION_ALARM_ON) {
//                val filePath = intent.getStringExtra(EXTRA_ALARM_URI) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
//                val volume = intent.getIntExtra(EXTRA_ALARM_VOLUME, 0)
//                val vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
//
//                if(RingtoneManager.getRingtone(applicationContext, Uri.parse(filePath)) == null) {
//                    Log.w(TAG, "onStartCommand: Cannot find ringtone at $filePath. Setting uri to default alarm ringtone.")
//                }
//
//                if(volume > 0) {
//                    if(mediaPlayer?.isPlaying == true) {
//                        mediaPlayer!!.stop()
//                    }
//
//                    mediaPlayer = MediaPlayer.create(this, Uri.parse(filePath)).apply {
//                        stop()
//                        isLooping = true
//                        setAudioAttributes(
//                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
//                        )
//                        prepare()
//                    }
//                }
//
//                if(vibrate) {
//                    vibrator = getSystemService(Vibrator::class.java)
//                }
//
//                playRingtone(volume, vibrate)
//            } else {
//                val notificationID: Int = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_DEFAULT_ID)
//                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.cancel(notificationID)
//                stopRingtone()
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }
//
//    private fun playRingtone(volume: Int, vibrate: Boolean) {
//        Log.d(TAG, "playRingtone: ")
//        if(volume > 0) {
//            if (mediaPlayer?.isPlaying == false) {
//                changeAudioState(volume)
//                mediaPlayer?.start()
//            } else {
//                Log.d(TAG, "playRingtone: Alarm is already playing")
//            }
//        }
//
//        if(vibrate) {
//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                vibrator?.vibrate(longArrayOf(1000, 500), 0)
//            } else {
//                vibrator?.vibrate(
//                    VibrationEffect.createWaveform(longArrayOf(1000, 500), intArrayOf(255, 0), 0))
//            }
//        }
//    }
//
//    private fun stopRingtone() {
//        Log.d(TAG, "stopRingtone: ")
//        if(mediaPlayer?.isPlaying == true) {
//            mediaPlayer?.stop()
//            restoreAudioState()
//        } else {
//            Log.d(TAG, "stopRingtone: Alarm is not playing")
//        }
//
//        vibrator?.cancel()
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancelAll()
//    }
//
//    private fun changeAudioState(volume: Int) {
//        savedRingerMode = audioManager.ringerMode
//        savedVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
//
//        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
//        audioManager.setStreamVolume(
//            AudioManager.STREAM_NOTIFICATION,
//            (audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) * (volume / 100.0)).toInt(),
//            0)
//    }
//
//    private fun restoreAudioState() {
//        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, savedVolume, 0)
//        audioManager.ringerMode = savedRingerMode
//    }
//
//    companion object {
//        const val ACTION_ALARM_ON: String = "com.example.moti.alarm.ACTION_ALARM_ON"
//        const val ACTION_ALARM_OFF: String = "com.example.moti.alarm.ACTION_ALARM_OFF"
//        const val EXTRA_ALARM_URI: String = "com.example.moti.alarm.EXTRA_ALARM_URI"
//        const val EXTRA_ALARM_VOLUME: String = "com.example.moti.alarm.EXTRA_ALARM_VOLUME"
//        const val EXTRA_ALARM_VIBRATE: String = "com.example.moti.alarm.EXTRA_ALARM_VIBRATE"
//        const val EXTRA_NOTIFICATION_ID: String = "com.example.moti.alarm.EXTRA_NOTIFICATION_ID"
//        const val NOTIFICATION_DEFAULT_ID: Int = 333333333
//    }
//}
