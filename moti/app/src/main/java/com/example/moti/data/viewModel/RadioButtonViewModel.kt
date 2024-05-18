package com.example.moti.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RadioButtonViewModel : ViewModel() {
    private val _selectedOption = MutableLiveData<Int>()
    val selectedOption: LiveData<Int> get() = _selectedOption

    fun setSelectedOption(option: Int) {
        _selectedOption.value = option
    }
}