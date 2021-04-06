package com.paytheory.android.example.ui.cash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CashViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Cash Fragment"
    }
    private val _message = MutableLiveData<String>().apply {
        value = "Cash coming soon"
    }
    val text: LiveData<String> = _text

    val message: LiveData<String> = _message
}