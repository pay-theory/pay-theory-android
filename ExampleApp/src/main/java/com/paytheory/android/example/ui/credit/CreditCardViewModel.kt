package com.paytheory.android.example.ui.credit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Example ViewModel
 */
class CreditCardViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Credit card Fragment"
    }

    val text: LiveData<String> = _text
}