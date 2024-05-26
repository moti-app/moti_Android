package com.example.moti.data.entity

import android.graphics.Bitmap
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
    var repeatDay : List<Week>?,
    var hasBanner : Boolean,
    var tagColor : TagColor?,
    var lastNoti : LocalDateTime?,
    var interval : Int?,
    var image : Bitmap?
){
  constructor(
      title : String,
      context : String,
      location : Location,
      whenArrival : Boolean,
      radius : Double,
      isRepeat : Boolean,
      repeatDay : List<Week>?,
      hasBanner : Boolean,
      tagColor: TagColor?,
      lastNoti : LocalDateTime?,
      interval : Int? = 1440,
      image : Bitmap?)
          : this(0, title, context, location, whenArrival, radius, isRepeat,
      repeatDay, hasBanner, tagColor, lastNoti, interval, image)
}