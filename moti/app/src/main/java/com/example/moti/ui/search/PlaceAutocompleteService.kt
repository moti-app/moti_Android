package com.example.moti.ui.search

import com.example.moti.data.model.PlaceAutocompleteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceAutocompleteService {
    @GET("/maps/api/place/autocomplete/json")
    suspend fun getPlaceAutocomplete(
        @Query("input") input: String,
        @Query("components") country: String,
        @Query("language") language: String,
        @Query("key") key: String
    ): PlaceAutocompleteResponse
}
