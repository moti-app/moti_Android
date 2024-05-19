package com.example.moti.data.repository

import com.example.moti.BuildConfig
import com.example.moti.data.model.PlaceAutocompleteResponse
import com.example.moti.data.model.PlaceSearchResponse
import com.example.moti.ui.search.PlaceAutocompleteService
import com.example.moti.ui.search.PlaceSearchService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaceRepository {

    private val api = BuildConfig.PLACE_API_KEY
    private val BASE_URL = "https://maps.googleapis.com"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val placeAutocompleteService: PlaceAutocompleteService by lazy {
        retrofit.create(PlaceAutocompleteService::class.java)
    }

    private val placeSearchService: PlaceSearchService by lazy {
        retrofit.create(PlaceSearchService::class.java)
    }

    suspend fun getPlaceAutocomplete(input: String, countryCode: String, languageCode: String): PlaceAutocompleteResponse {
        return placeAutocompleteService.getPlaceAutocomplete(input, countryCode, languageCode, api)
    }

    suspend fun getPlaceSearch(query: String, countryCode: String, languageCode: String): PlaceSearchResponse {
        return placeSearchService.getPlaceSearch(query, countryCode, languageCode, api)
    }
}
