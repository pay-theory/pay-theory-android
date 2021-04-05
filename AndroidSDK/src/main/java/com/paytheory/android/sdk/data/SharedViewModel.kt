package com.paytheory.android.sdk.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    val apiKey = MutableLiveData<String>()
    val amount = MutableLiveData<Int>()
    val accountNameEnabled = MutableLiveData<Boolean>()

    fun setApiKey(key: String) {
        apiKey.value = key
    }

    fun setAmount(paymentAmount: Int) {
        amount.value = paymentAmount
    }

    fun setAccountNameField(enableAccountName: Boolean) {
        accountNameEnabled.value = enableAccountName
    }
}


