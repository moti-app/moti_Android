package com.example.moti.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["tagTitle"], unique = true)])
data class Tag (
    @PrimaryKey(autoGenerate = true)
    var tagId : Long,
    @ColumnInfo(name = "tagTitle")
    var tagTitle : String
){
    constructor(tagTitle : String) :this(0, tagTitle)
}