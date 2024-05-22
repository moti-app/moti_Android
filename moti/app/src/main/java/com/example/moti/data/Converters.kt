package com.example.moti.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.moti.data.entity.Week
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter




class LocalDateTimeConverter {

    /**LocalDateTime*/
    @TypeConverter
    fun localDateTimeToString(value : LocalDateTime): String{
        return value.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun stringToLocalDateTime(value: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        return LocalDateTime.parse(value, formatter)
    }

    /**List*/
    @TypeConverter
    fun listToJson(value:List<Week>?): String?{
        return Gson().toJson(value)
    }
    @TypeConverter
    fun jsonToList(value : String): List<Week>?{
        return Gson().fromJson(value, Array<Week>::class.java)?.toList()
    }
}