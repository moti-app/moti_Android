package com.example.moti.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
    constructor( isSaved : Boolean,
                 createdAt : LocalDateTime,
                 updatedAt : LocalDateTime,
                 location : Location)
    :this(0, isSaved, createdAt, updatedAt, location)
}
