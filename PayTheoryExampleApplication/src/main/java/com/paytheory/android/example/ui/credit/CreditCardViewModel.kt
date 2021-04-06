package com.paytheory.android.example.ui.credit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CreditCardViewModel(application: Application) : AndroidViewModel(application) {
    private val _text = MutableLiveData<String>().apply {
        value = "Credit card Fragment"
    }

    val text: LiveData<String> = _text
}