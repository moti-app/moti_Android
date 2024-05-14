package com.example.moti.data.repository.dto

import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Tag

data class AlarmDetail(
    var alarm : Alarm,
    var tags : List<Tag>
)
