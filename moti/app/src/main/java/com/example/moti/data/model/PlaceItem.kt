package com.example.moti.data.model

data class PlaceItem(
    val title: String,
    val contents: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val id: Long = 0
)
