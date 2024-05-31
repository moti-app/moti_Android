package com.example.moti.ui.afterAction

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.moti.R

class AfterAction : AppCompatActivity() {

    private lateinit var silentDetailTextView: TextView
    private lateinit var silentActionSwitch: SwitchCompat
    private lateinit var openAppTextView: TextView
    private lateinit var openAppSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_action)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        // 스위치와 텍스트뷰 초기화
        silentDetailTextView = findViewById(R.id.silentDetailTextView)
        silentActionSwitch = findViewById(R.id.silentAction)
        openAppTextView = findViewById(R.id.openAppDetailTextView)
        openAppSwitch = findViewById(R.id.openAppAction)

        // 스위치 상태 변화 감지
        silentActionSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 스위치가 켜진 상태일 때
                silentDetailTextView.text = "켜짐"
            } else {
                // 스위치가 꺼진 상태일 때
                silentDetailTextView.text = "꺼짐"
            }
        }

        // 스위치 상태 변화 감지
        silentActionSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 스위치가 켜진 상태일 때
                silentDetailTextView.text = "켜짐"
                
            } else {
                // 스위치가 꺼진 상태일 때
                silentDetailTextView.text = "없음"
            }
        }
    }



}