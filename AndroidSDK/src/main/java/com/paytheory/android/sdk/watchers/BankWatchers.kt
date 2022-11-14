package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText


var accountNumberValid: Boolean = false
var routingNumberValid: Boolean = false
var bankFieldsValid: Boolean = false

private fun areFieldsValid(button: PayTheoryButton){
    //check if all card fields are valid
    bankFieldsValid = accountNumberValid && routingNumberValid
    //if all card fields are valid enable
    if (bankFieldsValid){
        button.enable()
    } else {
        button.disable()
    }
}



/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RoutingNumberTextWatcher(pt: PayTheoryEditText, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }

        val isValidNumber = s.toString().length == 9
        handleButton(isValidNumber)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            routingNumberValid = false
            ptText!!.error = "Invalid Routing Number"
        } else {
            routingNumberValid = true
        }
        areFieldsValid(submitButton)
    }
}


/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AccountNumberTextWatcher(pt: PayTheoryEditText, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }

        val isValidNumber = s.toString().length >= 5
        handleButton(isValidNumber)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            accountNumberValid = false
            ptText!!.error = "Invalid Account Number"
        } else {
            accountNumberValid = true
        }
        areFieldsValid(submitButton)
    }
}