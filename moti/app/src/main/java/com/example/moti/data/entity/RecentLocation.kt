package com.example.moti.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class RecentLocation(
    var isSaved : Boolean,
    var createdAt : LocalDateTime,
    var updatedAt : LocalDateTime,
    @Embedded
    var location : Location
){
    @PrimaryKey(autoGenerate = true)
    var recentLocationId : Long = 0
}
