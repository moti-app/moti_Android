package com.example.moti.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.RecentLocation

@Dao
interface RecentLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //primarykey 겹칠때 기존 데이터 삭제하고 새로운 데이터로 대체
    fun insert(recentLocation: RecentLocation)

    @Update
    fun update(recentLocation: RecentLocation)

    @Delete
    fun delete(recentLocation: RecentLocation)

    @Query("select * from RecentLocation")
    fun findAllRecentLocations(): List<RecentLocation>

    @Query("select * from RecentLocation order by updatedAt desc")
    fun findRecent10Locations():List<RecentLocation>

    @Query("select * from RecentLocation where recentLocationId = :recentLocationId")
    fun findRecentLocationById(recentLocationId: Long):RecentLocation

    @Query("select * from RecentLocation where isSaved = :isSaved")
    fun findSavedLocation(isSaved : Boolean):List<RecentLocation>

    @Query("select * from RecentLocation where address = :address")
    fun findByAddress(address : String):RecentLocation?
}