package com.example.moti.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.moti.data.entity.Week
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter




class Converters {

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

    /**Bitmap*/
    @TypeConverter
    fun uriToString(uri: Uri?):String?{
        return uri.toString()
    }

    @TypeConverter
    fun stringToUri(value: String?):Uri?{
        return Uri.parse(value)
    }
}