package com.example.moti.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update;

import com.example.moti.data.entity.Alarm;

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //primarykey 겹칠때 기존 데이터 삭제하고 새로운 데이터로 대체
    fun insert(alarm:Alarm):Long

    @Update
    fun update(alarm:Alarm)

    @Delete
    fun delete(alarm:Alarm)

    @Delete
    fun deleteAlarms(alarms:List<Alarm>)

    @Transaction
    @Query("select * from alarm")
    fun findAllAlarms():List<Alarm>

    @Transaction
    @Query("select * from alarm where alarmId = :alarmId")
    fun findAlarmById(alarmId:Long):Alarm
}
