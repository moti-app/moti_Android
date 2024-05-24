package com.example.moti.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm triggered!")
        Toast.makeText(context, "Alarm triggered!", Toast.LENGTH_LONG).show()
        // 여기서 원하는 작업을 수행할 수 있습니다.
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}