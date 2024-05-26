package com.example.moti.ui.alarm

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moti.R
import com.example.moti.data.Alarmtone
import com.better.alarm.ui.ringtonepicker.getPickedRingtone
import com.better.alarm.ui.ringtonepicker.userFriendlyTitle

class alarmCategory : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtonBanner: RadioButton
    private lateinit var radioButtonFullscreen: RadioButton
    private lateinit var layoutNotificationSound: LinearLayout
    private lateinit var selectedAlarmtone: Alarmtone

    companion object {
        private const val RINGTONE_PICKER_REQUEST_CODE = 999
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_notification_settings)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        radioGroup = findViewById(R.id.radioGroupNotificationType)
        radioButtonBanner = findViewById(R.id.radioButtonBanner)
        radioButtonFullscreen = findViewById(R.id.radioButtonFullscreen)
        layoutNotificationSound = findViewById(R.id.layoutNotificationSound)

        val hasBanner = intent.getBooleanExtra("hasBanner", true)
        val alarmtone = intent.getStringExtra("alarmtone")
        selectedAlarmtone = if (alarmtone != null) {
            Alarmtone.fromString(alarmtone)
        } else {
            Alarmtone.Default
        }
        if (hasBanner) {
            radioButtonBanner.isChecked = true
            showAdditionalSettings(false)
        } else {
            radioButtonFullscreen.isChecked = true
            showAdditionalSettings(true)
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val resultIntent = Intent()
            when (checkedId) {
                R.id.radioButtonFullscreen -> {
                    showAdditionalSettings(true)
                    resultIntent.putExtra("hasBanner", false)
                }
                R.id.radioButtonBanner -> {
                    showAdditionalSettings(false)
                    resultIntent.putExtra("hasBanner", true)
                }
            }
        }

        layoutNotificationSound.setOnClickListener {
            showRingtonePicker(selectedAlarmtone, RINGTONE_PICKER_REQUEST_CODE)
        }

        selectedAlarmtone = Alarmtone.Default
    }

    private fun showAdditionalSettings(show: Boolean) {
        val layoutNotificationSound: View = findViewById(R.id.layoutNotificationSound)
        val layoutVibration: View = findViewById(R.id.layoutVibration)
        val divider3: View = findViewById(R.id.divider3)
        val divider4: View = findViewById(R.id.divider4)

        if (show) {
            layoutNotificationSound.visibility = View.VISIBLE
            layoutVibration.visibility = View.VISIBLE
            divider3.visibility = View.VISIBLE
            divider4.visibility = View.VISIBLE
        } else {
            layoutNotificationSound.visibility = View.GONE
            layoutVibration.visibility = View.GONE
            divider3.visibility = View.GONE
            divider4.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedAlarmtone = data.getPickedRingtone()
            val alarmToneTitle = selectedAlarmtone.userFriendlyTitle(this)
            findViewById<TextView>(R.id.secondaryText3).text = alarmToneTitle

            val resultIntent = Intent().apply {
                putExtra("selectedAlarmtone", selectedAlarmtone.asString())
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }

    private fun showRingtonePicker(current: Alarmtone, ringtonePickerRequestCode: Int) {
        try {
            val pickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)

                val currentUri = when (current) {
                    is Alarmtone.Default -> Uri.parse(Alarmtone.defaultAlarmAlertUri)
                    is Alarmtone.Sound -> Uri.parse(current.uriString)
                    is Alarmtone.SystemDefault -> Uri.parse(Alarmtone.defaultAlarmAlertUri)
                    else -> null
                }

                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)

                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Uri.parse(Alarmtone.defaultAlarmAlertUri))
            }
            startActivityForResult(pickerIntent, ringtonePickerRequestCode)
        } catch (e: Exception) {
            Toast.makeText(this, "No Ringtone provider found…", Toast.LENGTH_LONG).show()
        }
    }
}
