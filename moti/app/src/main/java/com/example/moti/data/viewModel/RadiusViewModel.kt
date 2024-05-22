package com.example.moti.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RadiusViewModel : ViewModel() {
    private val _radius = MutableLiveData<Double>()
    val radius: LiveData<Double> get() = _radius

    fun setRadius(value: Double) {
        _radius.value = value
    }
}