package com.example.moti.data.entity

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.moti.data.Alarmtone
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
    var image : Uri?,
    @TypeConverters(AlarmtoneConverter::class)
    var alarmtone: Alarmtone?,
    var useVibration : Boolean,
    var isSleep : Boolean,
    var isOnAppMode: Boolean, //앱열기 상태 저장
    var isMessageMode: Boolean, //메시지 상태 저장
    var isSilentMode: Boolean, // 무음 모드 상태 저장
    var phoneNumber: String?, // 전화번호 저장
    var message: String?, // 문자 내역 저장
    var appPackageName: String? // 앱 패키지명 저장

) {
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
        image : Uri?,
        alarmtone: Alarmtone?,
        useVibration : Boolean,
        isSleep: Boolean,
        isOnAppMode: Boolean,
        isMesaageMode: Boolean,
        isSilentMode: Boolean,
        phoneNumber: String?,
        message: String?,
        appPackageName: String?
    ) : this(
        0,
        title,
        context,
        location,
        whenArrival,
        radius,
        isRepeat,
        repeatDay,
        hasBanner,
        tagColor,
        lastNoti,
        interval,
        image,
        alarmtone,
        useVibration,
        isSleep,
        isOnAppMode,
        isMesaageMode,
        isSilentMode,
        phoneNumber,
        message,
        appPackageName
    )
}