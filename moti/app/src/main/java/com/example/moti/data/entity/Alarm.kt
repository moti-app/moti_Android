package com.example.moti.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Alarm")
data class Alarm (
    var title : String,
    var context : String,
    @Embedded
    var location : Location,
    var whenArrival : Boolean,
    var radius : Double,
    var isRepeat : Boolean,
    var repeatDay : Week,
    var hasBanner : Boolean
){
    @PrimaryKey(autoGenerate = true)
    var alarmId : Long = 0
}