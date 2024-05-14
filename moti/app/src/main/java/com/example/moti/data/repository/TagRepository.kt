package com.example.moti.data.repository

import androidx.annotation.WorkerThread
import com.example.moti.data.dao.TagDao
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Tag

class TagRepository(private val tagDao: TagDao) {
    /**태그 리스트 생성*/
    @WorkerThread
    fun createTags(tags : List<Tag>){
        tags.map { tagDao.insert(it) }
    }

    /**태그 리스트 수정*/
    fun updateTags(tags : List<Tag>){
        tags.map {tagDao.update(it)}
    }
    /**태그 리스트 삭제*/
    fun deleteTags(tags : List<Tag>){
        tags.map { tagDao.delete(it) }
    }
    /**태그 조회*/
    fun findTagByAlarm(alarm : Alarm): List<Tag>{
        return tagDao.findTagsByAlarmId(alarm.alarmId)
    }
}