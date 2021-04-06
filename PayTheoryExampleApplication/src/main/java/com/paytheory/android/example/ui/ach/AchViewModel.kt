package com.paytheory.android.example.ui.ach

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AchViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "ACH Fragment"
    }
    val text: LiveData<String> = _text
}