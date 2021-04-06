package com.paytheory.android.example.ui.cash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CashViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is cash fragment"
    }
    val text: LiveData<String> = _text
}