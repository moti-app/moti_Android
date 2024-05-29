package com.example.moti.ui.addMemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moti.data.entity.TagColor

class TagColorViewModel : ViewModel() {
    private val _selectedTagColor = MutableLiveData<TagColor>()
    val selectedTagColor: LiveData<TagColor> get() = _selectedTagColor

    fun setSelectedTagColor(tagColor: TagColor) {
        _selectedTagColor.value = tagColor
    }
}
