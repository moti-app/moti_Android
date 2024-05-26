package com.example.moti.data.entity

import androidx.room.TypeConverter
import com.example.moti.data.Alarmtone

class AlarmtoneConverter {

    @TypeConverter
    fun fromAlarmtone(alarmtone: Alarmtone): String {
        return alarmtone.asString()
    }

    @TypeConverter
    fun toAlarmtone(alarmtoneString: String): Alarmtone {
        return Alarmtone.fromString(alarmtoneString)
    }
}
