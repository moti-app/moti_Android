package com.example.moti.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Alarm")
data class Alarm (
    @PrimaryKey(autoGenerate = true)
    var alarmId : Long,
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
  constructor(title : String,
              context : String,
              location : Location,
              whenArrival : Boolean,
              radius : Double,
              isRepeat : Boolean,
              repeatDay : Week,
              hasBanner : Boolean)
          : this(0, title, context, location, whenArrival, radius, isRepeat, repeatDay, hasBanner)
}