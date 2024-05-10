package com.example.moti.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.moti.data.entity.AlarmAndTag;

@Dao
interface AlarmAndTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //primarykey 겹칠때 기존 데이터 삭제하고 새로운 데이터로 대체
    fun insert(alarmAndTag:AlarmAndTag)

    @Update
    fun update(alarmAndTag:AlarmAndTag)

    @Delete
    fun delete(alarmAndTag:AlarmAndTag)

    @Delete
    fun delete(alarmAndTag: List<AlarmAndTag>)

    @Query("select * from AlarmAndTag")
    fun findAllAlarmAndTags():List<AlarmAndTag>

    @Query("select * from AlarmAndTag where alarmId = :id")
    fun findAllByAlarmId(id:Long):List<AlarmAndTag>
}
