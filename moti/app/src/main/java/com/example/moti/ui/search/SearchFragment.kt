package com.example.moti.ui.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.RecentLocation
import com.example.moti.data.model.PlaceItem
import com.example.moti.data.repository.RecentLocationRepository
import com.example.moti.databinding.FragmentSearchBinding
import com.example.moti.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

private const val ARG_PARAM1 = "param1"

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: PlacesRVAdapter
    private lateinit var db: MotiDatabase
    private lateinit var recentLocationRepository: RecentLocationRepository

    private var countryCode: String = "country:kr"
    private var languageCode: String = "ko"
    private var places:MutableList<PlaceItem> = mutableListOf()
    private var query:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val systemLocale: Locale = activity?.resources?.configuration?.locales?.get(0)!!
        countryCode = "country:" + systemLocale.country
        languageCode = systemLocale.language
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchViewModel.autocomplete(it.getString(ARG_PARAM1) ?: "", countryCode, languageCode)
        }
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        recentLocationRepository = RecentLocationRepository(db.recentLocationDao())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewLifecycleOwner
//        binding.lifecycleOwner = viewLifecycleOwner
//        binding.viewModel = searchViewModel

        setupPlacesRV()
        observeViewModel()

        return binding.root
    }

    private fun setupPlacesRV() {
        adapter = PlacesRVAdapter(places)
        binding.rvSearch.adapter = adapter
        binding.rvSearch.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvSearch.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))

        adapter.setItemClickListener(object : PlacesRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val placeItem = adapter.places[position]
                searchViewModel.searchPlaces("${placeItem.contents} ${placeItem.title}", countryCode, languageCode)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        searchViewModel.autocompleteList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        searchViewModel.searchResult.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                val address = it.results[0].formattedAddress
                val lat = it.results[0].geometry.location.lat
                val lng = it.results[0].geometry.location.lng

                val recentLocation = RecentLocation(com.example.moti.data.entity.Location(lat, lng, address, response.results[0].name))
                CoroutineScope(Dispatchers.IO).launch {
                    recentLocationRepository.createRecentLocation(recentLocation)
                }

                val intent = Intent(activity, MainActivity::class.java).apply {
                    putExtra("name", response.results[0].name)
                    putExtra("lat", lat.toString())
                    putExtra("lng", lng.toString())
                }
                activity?.setResult(RESULT_OK, intent)
                activity?.finish()
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(query: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, query)
                }
            }
    }
    fun updateQuery(query: String?) {
        if (query != null) {
            this.query = query
            searchViewModel.autocomplete(query,countryCode,languageCode)
        }
//        if (query != null) {
//            autocomplete(query)
//        }
    }
}
