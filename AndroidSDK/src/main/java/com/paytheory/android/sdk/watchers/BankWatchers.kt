package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText


var accountNumberValid: Boolean = false
var routingNumberValid: Boolean = false
var bankFieldsValid: Boolean = false

/*
* Modernization
* Watchers have been updated to support ValidAndEmpty protocol
* changes are in afterTextChanged
* */

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
class RoutingNumberTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt
    private var ptFragment: PayTheoryFragment? = fragment

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            ptFragment!!.ach.accountNumber.setEmpty(true)
            ptFragment!!.ach.accountNumber.setValid(false)
            return
        }

        val isValidNumber = s.toString().length == 9
        ptFragment!!.ach.routingNumber.setEmpty(false)
        ptFragment!!.ach.routingNumber.setValid(isValidNumber)
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
class AccountNumberTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt
    private var ptFragment: PayTheoryFragment? = fragment

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            ptFragment!!.ach.accountNumber.setEmpty(true)
            ptFragment!!.ach.accountNumber.setValid(false)
            return
        }

        val isValidNumber = s.toString().length >= 5
        ptFragment!!.ach.accountNumber.setEmpty(false)
        ptFragment!!.ach.accountNumber.setValid(isValidNumber)
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