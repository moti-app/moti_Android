package com.example.moti.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //primarykey 겹칠때 기존 데이터 삭제하고 새로운 데이터로 대체
    fun insert(tag: Tag):Long

    @Update
    fun update(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("select * from Tag")
    fun findAllTags(): List<Tag>

    @Query("select * from Tag natural join AlarmAndTag where alarmId = :alarmId")
    fun findTagsByAlarmId(alarmId:Long):List<Tag>
}