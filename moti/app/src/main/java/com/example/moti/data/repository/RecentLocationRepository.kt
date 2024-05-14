package com.example.moti.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.moti.data.dao.RecentLocationDao
import com.example.moti.data.entity.RecentLocation
import java.time.LocalDateTime

class RecentLocationRepository(private val recentLocationDao: RecentLocationDao) {
    /**검색 기록 추가*/
    fun createRecentLocation(recentLocation: RecentLocation){
        recentLocationDao.insert(recentLocation);
    }

    /**검색 기록 업데이트(같은 장소 다시 검색 시)*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateRecentLocation(recentLocation: RecentLocation){
        recentLocation.updatedAt = LocalDateTime.now();
        recentLocationDao.update(recentLocation);
    }

    /**검색 기록 삭제*/
    fun deleteRecentLocation(recentLocation: RecentLocation){
        recentLocationDao.delete(recentLocation);
    }

    /**최근 검색 10개 조회-몇 개까지 보이게 할건지 정해야 함 */
    fun findRecentLocation():List<RecentLocation>{
        return recentLocationDao.findRecent10Locations();
    }

    /**장소 저장 추가 (10개 제한)*/
    fun addSavedLocation(recentLocationId: Long){
        var recentLocation = recentLocationDao.findRecentLocationById(recentLocationId);
        var savedLocations = recentLocationDao.findSavedLocation(true);
        if(savedLocations.size < 11) {
            recentLocation.isSaved = true
            recentLocationDao.update(recentLocation);
        }
    }

    /**저장한 장소 삭제*/
    fun deleteSavedLocation(recentLocationId: Long){
        var recentLocation = recentLocationDao.findRecentLocationById(recentLocationId);
        if(recentLocation.isSaved) {
            recentLocation.isSaved = false
            recentLocationDao.update(recentLocation);
        }
    }


    /**저장한 장소 전체 조회*/
    fun findAllSavedLocation():List<RecentLocation>{
        return recentLocationDao.findSavedLocation(true)
    }
}