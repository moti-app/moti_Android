package com.example.moti.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moti.data.model.PlaceItem
import com.example.moti.data.model.PlaceSearchResponse
import com.example.moti.data.repository.PlaceRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = PlaceRepository()

    private val _autocompleteList = MutableLiveData<List<PlaceItem>>()
    val autocompleteList: LiveData<List<PlaceItem>> get() = _autocompleteList

    private val _searchResult = MutableLiveData<PlaceSearchResponse>()
    val searchResult: LiveData<PlaceSearchResponse> get() = _searchResult

    fun autocomplete(query: String, countryCode: String, languageCode: String) {
        viewModelScope.launch {
            try {
                val response = repository.getPlaceAutocomplete(query, countryCode, languageCode)
                _autocompleteList.value = response.predictions.map {
                    PlaceItem(it.structuredFormatting.mainText, it.structuredFormatting.secondaryText)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun searchPlaces(query: String, countryCode: String, languageCode: String) {
        viewModelScope.launch {
            try {
                val response = repository.getPlaceSearch(query, countryCode, languageCode)
                _searchResult.value = response
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
