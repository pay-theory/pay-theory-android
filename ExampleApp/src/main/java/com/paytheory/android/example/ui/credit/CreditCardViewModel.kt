package com.paytheory.android.example.ui.credit


import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.paytheory.android.example.R
import com.paytheory.android.sdk.fragments.PayTheoryFragment

class CreditCardViewModel : ViewModel() {
    private val _arguments = MutableLiveData<Bundle>().apply {
        value = Bundle()
        value!!.putString(PayTheoryFragment.API_KEY, R.string.pay_theory_api_key.toString())
        value!!.putInt(PayTheoryFragment.AMOUNT,5000)
        value!!.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,true)
        value!!.putBoolean(PayTheoryFragment.BILLING_ADDRESS_ENABLED,true)
    }
    private val _text = MutableLiveData<String>().apply {
        value = "This is credit card Fragment"
    }
    val arguments: LiveData<Bundle> = _arguments
    val text: LiveData<String> = _text
}