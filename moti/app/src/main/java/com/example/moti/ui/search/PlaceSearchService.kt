package com.example.moti.ui.search

import com.example.moti.data.model.PlaceSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceSearchService {
    @GET("/maps/api/place/textsearch/json")
    suspend fun getPlaceSearch(
        @Query("query") input: String,
        @Query("components") country: String,
        @Query("language") language: String,
        @Query("key") key: String
    ): PlaceSearchResponse
}
