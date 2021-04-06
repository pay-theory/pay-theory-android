package com.paytheory.android.sdk.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveDataViewModel : ViewModel() {

    private val apiKey = MutableLiveData<String>()
    private val amount = MutableLiveData<Int>()
    private val accountNameEnabled = MutableLiveData<Boolean>(false)
    private val achEnabled = MutableLiveData<Boolean>(false)
    private val billingAddressEnabled = MutableLiveData<Boolean>(false)

    val selectedApiKey: LiveData<String> get() = apiKey
    val selectedAmount: LiveData<Int> get() = amount
    val selectedAccountNameField: LiveData<Boolean> get() = accountNameEnabled
    val selectedAchEnabled: LiveData<Boolean> get() = achEnabled
    val selectedBillingAddressEnabled: LiveData<Boolean> get() = billingAddressEnabled


    fun setApiKey(key: String) {
        apiKey.value = key
    }

    fun setAmount(paymentAmount: Int) {
        amount.value = paymentAmount
    }

    fun setAccountNameField(enableAccountName: Boolean) {
        accountNameEnabled.value = enableAccountName
    }

    fun setAchFields(enableAch: Boolean) {
        achEnabled.value = enableAch
    }

    fun setBillingAddressFields(enableBillingAddress: Boolean) {
        billingAddressEnabled.value = enableBillingAddress
    }
}


