package com.example.moti.data.repository

import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import com.example.moti.data.dao.AlarmAndTagDao
import com.example.moti.data.dao.AlarmDao
import com.example.moti.data.dao.TagDao
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.AlarmAndTag
import com.example.moti.data.entity.Tag
import com.example.moti.data.repository.dto.AlarmDetail
import kotlin.streams.toList

class AlarmRepository(private val alarmDao: AlarmDao,
                      private val tagDao: TagDao,
                      private val alarmAndTagDao: AlarmAndTagDao
) {

    /**테스트 필요*/
    /**알람 생성*/
    @WorkerThread
    fun createAlarmAndTag(alarm : Alarm, tagIds: List<Long>){
        var alarmId = alarmDao.insert(alarm)
        var newAlarmAndTag = tagIds.map {AlarmAndTag(alarmId, it)}.toList()
        newAlarmAndTag.map { alarmAndTagDao.insert(it) }
    }

    /**모든 알람 조회*/
    @WorkerThread
    fun findAllAlarms():List<Alarm>{
        return alarmDao.findAllAlarms()
    }

    /**알람 상세 조회*/
    @WorkerThread
    fun findAlarm(alarmId : Long): AlarmDetail{
        var tags = tagDao.findTagsByAlarmId(alarmId)
        var alarm = alarmDao.findAlarmById(alarmId)
        return AlarmDetail(alarm, tags)
    }

    /**알람 수정*/
    @WorkerThread
    fun updateAlarm(alarm: Alarm, tagIds: List<Long>){
        alarmDao.update(alarm)
        var alarmAndTags = alarmAndTagDao.findAllByAlarmId(alarm.alarmId)
        alarmAndTagDao.delete(alarmAndTags)
        var newAlarmAndTag = tagIds.map {AlarmAndTag(alarm.alarmId, it)}.toList()
        newAlarmAndTag.map { alarmAndTagDao.insert(it) }
    }

    /**알람 리스트 삭제*/
    @RequiresApi(34)
    fun deleteAlarms(ids : List<Long>){
        var alarms : List<Alarm> = ids.map { id -> alarmDao.findAlarmById(id) }.toList()
        var alarmAndTags = alarms.map { alarmAndTagDao.findAllByAlarmId(it.alarmId)}
        alarmAndTags.map{alarmAndTagDao.delete(it)}
        alarmDao.deleteAlarms(alarms)
    }
}