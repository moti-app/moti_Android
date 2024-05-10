package com.example.moti.data.entity

import androidx.room.Entity
import java.time.LocalDateTime

@Entity
data class RecentLocation(
    var isSaved : Boolean,
    var createdAt : LocalDateTime,
    var updatedAt : LocalDateTime,
    var location : Location
)
