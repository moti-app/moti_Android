package com.example.moti.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag (
    var tagTitle : String
){
    @PrimaryKey(autoGenerate = true)
    var tagId : Long = 0
}