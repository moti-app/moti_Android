package com.example.moti.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.moti.data.entity.Week
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
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

    /**Bitmap*/
    @TypeConverter
    fun bitmapToByteArray(bitmap : Bitmap):ByteArray{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun byteArrayToBitmap(bytes : ByteArray):Bitmap{
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}