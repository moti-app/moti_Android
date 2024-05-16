package com.example.moti.ui.search

import android.util.Log
import com.example.moti.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class ReverseGeocoding(private val listener: ReverseGeocodingListener) {
    interface ReverseGeocodingListener {
        fun onReverseGeocodeSuccess(address: String)
        fun onReverseGeocodeFailure(errorMessage: String)
    }
    private val BASE_URL = "https://maps.googleapis.com"
    private val API_KEY = BuildConfig.PLACE_API_KEY
    private val TAG = "placeApi"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    fun reverseGeocode(latlng: String, language: String) {
        val service = retrofit.create(GeocodingService::class.java)
        val call = service.reverseGeocode(latlng, API_KEY,language)

        call.enqueue(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                if (response.isSuccessful) {
                    val geocodingResponse = response.body()
                    if (geocodingResponse != null && geocodingResponse.results.isNotEmpty()) {
                        val address = geocodingResponse.results[0].formatted_address
                        listener.onReverseGeocodeSuccess(address)
                    } else {
                        listener.onReverseGeocodeFailure("No results found")
                    }
                } else {
                    listener.onReverseGeocodeFailure("Unsuccessful response")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message}", t)
            }
        })
    }
}

interface GeocodingService {
    @GET("/maps/api/geocode/json")
    fun reverseGeocode(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String,
        @Query("language") language:String
    ): Call<GeocodingResponse>
}

data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String
)

data class GeocodingResult(
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String,
    val types: List<String>,
    val address_components: List<AddressComponent>
)

data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)
