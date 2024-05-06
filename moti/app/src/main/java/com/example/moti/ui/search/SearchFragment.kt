package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.R
import com.example.moti.databinding.FragmentSearchBinding
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val ARG_PARAM1 = "param1"
class SearchFragment : Fragment() {

    private val TAG = "placeApi"
    private val BASE_URL = "https://maps.googleapis.com"
    private val API_KEY = "APIKEY"
    private val COUNTRY_CODE = "country:kr"


    // TODO: Rename and change types of parameters
    private var query: String? = null
    private lateinit var binding: FragmentSearchBinding

    private lateinit var adapter: PlacesRVAdapter
    private var autocompleteList = mutableListOf<PlaceItem>()

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

    private val placeAutocompleteService: PlaceAutocompleteService by lazy {
        retrofit.create(PlaceAutocompleteService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            query = it.getString(ARG_PARAM1)
        }
        adapter = PlacesRVAdapter(autocompleteList)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentSearchBinding.inflate(layoutInflater)
        setupPlacesRV()
        query?.let { autocomplete(it) }
        return binding.root
    }

    companion object {
        @JvmStatic fun newInstance(query: String) =
                SearchFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, query)
                    }
                }
    }
    fun updateQuery(query: String?) {
        this.query = query
        if (query != null) {
            autocomplete(query)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupPlacesRV() {
        binding.rvSearch.adapter = adapter
        adapter.notifyDataSetChanged()
        binding.rvSearch.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun searchPlaces(query: String) {
        val service = retrofit.create(PlaceSearchService::class.java)
        val call = service.getPlaceSearch(query, COUNTRY_CODE,"ko", API_KEY)

        call.enqueue(object : Callback<PlaceSearchResponse> {
            override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                if (response.isSuccessful) {
                    val name = response.body()?.results?.get(0)?.name
                    val lat = response.body()?.results?.get(0)?.geometry?.location?.lat
                    val lng = response.body()?.results?.get(0)?.geometry?.location?.lng
                    Log.d("lat","$lat")
                    Log.d("lng","$lng")
                } else {
                    Log.e(TAG, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message}", t)
            }
        })
    }

    interface PlaceSearchService {
        @GET("/maps/api/place/textsearch/json")
        fun getPlaceSearch(
            @Query("query") input: String,
            @Query("components") country: String,
            @Query("language") language:String,
            @Query("key") key: String
        ): Call<PlaceSearchResponse>
    }

    data class PlaceSearchResponse(
        val results: List<PlaceResult>,
        val status: String
    )

    data class PlaceResult(
        @SerializedName("formatted_address") val formattedAddress: String,
        val geometry: Geometry,
        val icon: String,
        @SerializedName("icon_background_color") val iconBackgroundColor: String,
        @SerializedName("icon_mask_base_uri") val iconMaskBaseUri: String,
        val name: String,
        @SerializedName("place_id") val placeId: String,
        @SerializedName("plus_code") val plusCode: PlusCode,
        val reference: String,
        val types: List<String>
    )

    data class Geometry(
        val location: Location,
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

    data class PlusCode(
        @SerializedName("compound_code") val compoundCode: String,
        @SerializedName("global_code") val globalCode: String
    )




    private fun autocomplete(input: String) {
        val call = placeAutocompleteService.getPlaceAutocomplete(input, COUNTRY_CODE,"ko", API_KEY)

        call.enqueue(object : Callback<PlaceAutocompleteResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<PlaceAutocompleteResponse>,
                response: Response<PlaceAutocompleteResponse>
            ) {
                if (response.isSuccessful) {
                    val predictions = response.body()?.predictions ?: emptyList()
                    autocompleteList.clear()
                    Log.e(TAG, "Count: ${predictions.count()}")
                    for (prediction in predictions) {
                        Log.e(TAG, "AAA: 1, ${prediction.description}")
                        val placeId = prediction.placeId
                        val description = prediction.description
                        val main = prediction.structuredFormatting.mainText
                        val second = prediction.structuredFormatting.secondaryText
                        autocompleteList.add(PlaceItem(main, second, R.drawable.ic_launcher_background))
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e(TAG, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlaceAutocompleteResponse>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message}", t)
            }
        })
    }

}
    interface PlaceAutocompleteService {
        @GET("/maps/api/place/autocomplete/json")
        fun getPlaceAutocomplete(
            @Query("input") input: String,
            @Query("components") country: String,
            @Query("language") language:String,
            @Query("key") key: String
        ): Call<PlaceAutocompleteResponse>
    }
    data class PlaceAutocompleteResponse(
        @SerializedName("predictions")
        val predictions: List<Prediction>
    )

    data class Prediction(
        @SerializedName("place_id")
        val placeId: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("structured_formatting")
        val structuredFormatting: StructuredFormatting

    )
    data class StructuredFormatting(
        @SerializedName("main_text")
        val mainText: String,
        @SerializedName("secondary_text")
        val secondaryText: String
    )