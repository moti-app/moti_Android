package com.example.moti.data.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

@Entity
data class RecentLocation(
    @PrimaryKey(autoGenerate = true)
    var recentLocationId : Long,
    var isSaved : Boolean,
    var createdAt : LocalDateTime,
    var updatedAt : LocalDateTime,
    @Embedded
    var location : Location
){
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(isSaved : Boolean,
                location : Location)
    :this(0, false, LocalDateTime.now(), LocalDateTime.now(), location)
}
