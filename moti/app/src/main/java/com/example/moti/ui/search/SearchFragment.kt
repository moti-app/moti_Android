package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.BuildConfig
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.RecentLocation
import com.example.moti.data.repository.RecentLocationRepository
import com.example.moti.databinding.FragmentSearchBinding
import com.example.moti.ui.main.MainActivity
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale


private const val ARG_PARAM1 = "param1"
class SearchFragment : Fragment() {

    private val api = BuildConfig.PLACE_API_KEY

    private val tag = "placeApi"
    private val url = "https://maps.googleapis.com"
    private val apiKey = api
    private var countryCode = "country:kr"
    private var languageCode = "ko"


    // TODO: Rename and change types of parameters
    private var query: String? = null
    private lateinit var binding: FragmentSearchBinding

    private lateinit var adapter: PlacesRVAdapter
    private var autocompleteList = mutableListOf<PlaceItem>()
    private lateinit var db: MotiDatabase
    private lateinit var recentLocationRepository: RecentLocationRepository

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(url)
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
        val systemLocale: Locale = activity?.resources?.configuration?.locales?.get(0)!!
        countryCode = "country:" + systemLocale.country
        languageCode = systemLocale.language
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        recentLocationRepository = RecentLocationRepository(db.recentLocationDao())

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

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
        val decoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        binding.rvSearch.addItemDecoration(decoration)
//        binding.rvSearch.addOnScrollListener(object  : RecyclerView.OnScrollListener() {
//
//        })
        adapter.setItemClickListener(object : PlacesRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                searchPlaces(autocompleteList[position].contents,autocompleteList[position].title)
            }

        })
        binding.rvSearch.adapter = adapter
        adapter.notifyDataSetChanged()
        binding.rvSearch.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun searchPlaces(query: String,place:String) {
        val service = retrofit.create(PlaceSearchService::class.java)
        val call = service.getPlaceSearch("$place $query", countryCode,languageCode, apiKey)

        call.enqueue(object : Callback<PlaceSearchResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<PlaceSearchResponse>, response: Response<PlaceSearchResponse>) {
                if (response.isSuccessful) {
                    val address = response.body()?.results?.get(0)?.formattedAddress ?: "unknown"
                    val lat = response.body()?.results?.get(0)?.geometry?.location?.lat
                    val lng = response.body()?.results?.get(0)?.geometry?.location?.lng

                    val recentLocation = RecentLocation(com.example.moti.data.entity.Location(lat!!,lng!!,address,place))

                    CoroutineScope(Dispatchers.IO).launch {
                        recentLocationRepository.createRecentLocation(recentLocation)
                    }

                    val intent = Intent(activity, MainActivity::class.java)

                    intent.putExtra("name",place)
                    intent.putExtra("address",address)
                    intent.putExtra("lat",lat.toString())
                    intent.putExtra("lng",lng.toString())
                    activity!!.setResult(RESULT_OK, intent)
                    activity!!.finish()
                }
            }

            override fun onFailure(call: Call<PlaceSearchResponse>, t: Throwable) {
                Log.e(tag, "Error: ${t.message}", t)
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
        val call = placeAutocompleteService.getPlaceAutocomplete(input, countryCode,languageCode, apiKey)

        call.enqueue(object : Callback<PlaceAutocompleteResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<PlaceAutocompleteResponse>,
                response: Response<PlaceAutocompleteResponse>
            ) {
                if (response.isSuccessful) {
                    val predictions = response.body()?.predictions ?: emptyList()
                    autocompleteList.clear()
                    Log.e(tag, "Count: ${predictions.count()}")
                    for (prediction in predictions) {
                        Log.e(tag, "AAA: 1, ${prediction.description}")
                        val main = prediction.structuredFormatting.mainText
                        val second = prediction.structuredFormatting.secondaryText ?: "" // ?: is necessary
                        autocompleteList.add(PlaceItem(main, second,1.0,1.0, 0)) // lat, lng, id are not important, dummy data
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e(tag, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PlaceAutocompleteResponse>, t: Throwable) {
                Log.e(tag, "Error: ${t.message}", t)
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