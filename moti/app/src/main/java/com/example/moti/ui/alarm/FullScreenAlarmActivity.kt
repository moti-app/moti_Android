package com.example.moti.ui.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moti.R
import com.example.moti.alarm.FullScreenAlarmService
import com.example.moti.data.MotiDatabase
import com.example.moti.databinding.ActivityFullScreenAlarmBinding
import com.example.moti.databinding.FragmentMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FullScreenAlarmActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFullScreenAlarmBinding

    private var notificationId: Int = 0
    private var alarmId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFullScreenAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("aa", "full onCreate")

        notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        alarmId = intent.getLongExtra("AlarmID", 0)

        // Set an onClickListener for the button
        binding.endBtn.setOnClickListener {
            stopAlarmService()
            dismissNotification()
            finish() // Close the activity when the button is clicked
        }

        turnScreenOnAndKeyguardOff()
        loadAlarmDetails()
    }

    private fun loadAlarmDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = MotiDatabase.getInstance(applicationContext)
            val alarm = database?.alarmDao()?.findAlarmById(alarmId)
            if (alarm != null) {
                runOnUiThread {
                    binding.alarmTitleTv.text = alarm.title
                    binding.LocationTv.text = alarm.context
                    binding.whenTv.text = if (alarm.whenArrival) "도착할때" else "떠날때"
                    if(alarm.image!=null){
                        binding.alarmImg.setImageURI(alarm.image)
                    }
                }
            }
        }
    }

    private fun stopAlarmService() {
        val intent = Intent(this, FullScreenAlarmService::class.java).apply {
            action = FullScreenAlarmService.ACTION_ALARM_OFF
            putExtra(FullScreenAlarmService.EXTRA_NOTIFICATION_ID, notificationId)
        }
        startService(intent)
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED    // deprecated api 27
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD     // deprecated api 26
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   // deprecated api 27
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
        val keyguardMgr = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardMgr.requestDismissKeyguard(this, null)
        }
    }

    private fun dismissNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        Log.e("aa", "full cancel")
    }
}
