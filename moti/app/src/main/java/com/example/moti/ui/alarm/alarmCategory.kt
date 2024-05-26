package com.example.moti.ui.alarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.moti.R

class alarmCategory : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtonBanner: RadioButton
    private lateinit var radioButtonFullscreen: RadioButton

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

        // Intent로 전달된 hasBanner 값을 가져와서 라디오 버튼의 선택 상태를 설정
        val hasBanner = intent.getBooleanExtra("hasBanner", true)
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
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }

    private fun showAdditionalSettings(show: Boolean) {
        val layoutNotificationSound: View = findViewById(R.id.layoutNotificationSound)
        val layoutVibration: View = findViewById(R.id.layoutVibration)
        val layoutRepeatNotification: View = findViewById(R.id.layoutRepeatNotification)
        val divider3: View = findViewById(R.id.divider3)
        val divider4: View = findViewById(R.id.divider4)
        val divider5: View = findViewById(R.id.divider5)

        if (show) {
            layoutNotificationSound.visibility = View.VISIBLE
            layoutVibration.visibility = View.VISIBLE
            layoutRepeatNotification.visibility = View.VISIBLE
            divider3.visibility = View.VISIBLE
            divider4.visibility = View.VISIBLE
            divider5.visibility = View.VISIBLE
        } else {
            layoutNotificationSound.visibility = View.GONE
            layoutVibration.visibility = View.GONE
            layoutRepeatNotification.visibility = View.GONE
            divider3.visibility = View.GONE
            divider4.visibility = View.GONE
            divider5.visibility = View.GONE
        }
    }
}

