package com.example.moti.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(primaryKeys = arrayOf("alarmId","tagId"),
    foreignKeys = arrayOf(
        ForeignKey(entity = Alarm::class,
            parentColumns = arrayOf("alarmId"),
            childColumns = arrayOf("alarmId")),
        ForeignKey(entity = Tag::class,
            parentColumns = arrayOf("tagId"),
            childColumns = arrayOf("tagId"))
    )
)
data class AlarmAndTag(
    val alarmId : Long,
    val tagId : Long
)
