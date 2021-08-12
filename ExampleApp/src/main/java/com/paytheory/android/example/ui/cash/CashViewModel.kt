package com.paytheory.android.example.ui.cash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Example ViewModel
 */
class CashViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Cash Fragment"
    }
    val text: LiveData<String> = _text

}