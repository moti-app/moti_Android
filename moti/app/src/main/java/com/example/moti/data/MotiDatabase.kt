package com.example.moti.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moti.data.dao.AlarmAndTagDao
import com.example.moti.data.dao.AlarmDao
import com.example.moti.data.dao.RecentLocationDao
import com.example.moti.data.dao.TagDao
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.AlarmAndTag
import com.example.moti.data.entity.Location
import com.example.moti.data.entity.RecentLocation
import com.example.moti.data.entity.Tag

@Database(entities = [Alarm::class, AlarmAndTag::class, RecentLocation::class, Tag::class],
    version = 5)
@TypeConverters(LocalDateTimeConverter::class)
abstract class MotiDatabase :RoomDatabase(){
    abstract fun alarmDao():AlarmDao
    abstract fun recentLocationDao():RecentLocationDao
    abstract fun tagDao():TagDao
    abstract fun alarmAndTagDao():AlarmAndTagDao
    companion object{
        private var instance: MotiDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MotiDatabase?{
            if(instance==null){
                synchronized(MotiDatabase::class){
                    instance= Room.databaseBuilder(
                        context.applicationContext,
                        MotiDatabase::class.java,
                        "moti-database"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }
}