package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText


/**
 * Boolean that tracks the validity of the account number
 */
var accountNumberValid: Boolean = false
/**
 * Boolean that tracks the validity of the routing number
 */
var routingNumberValid: Boolean = false
/**
 * Boolean that tracks the validity of all bank fields
 */
var bankFieldsValid: Boolean = false

/**
 * Function that checks the validity of all bank fields and enables/disables the pay button
 * @param button pay theory button
 */
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

    /**
     * Function that handles text changes
     * @param s editable text
     * @param start start index
     * @param before char count before change
     * @param count char count after change
     */
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes before they happen
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes after they happen
     */
    override fun afterTextChanged(editable: Editable) {
        val s = editable.toString()
        if (s.isEmpty()) {
            ptFragment!!.ach.routingNumber.setEmpty(true)
            ptFragment!!.ach.routingNumber.setValid(false)
            return
        }

        val isValidNumber = s.toString().length == 9
        ptFragment!!.ach.routingNumber.setEmpty(false)
        ptFragment!!.ach.routingNumber.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    /**
     * Function that that handles the pay button and field errors
     * @param valid boolean that determines if the routing number is valid
     */
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

    /**
     * Function that handles text changes
     * @param s editable text
     * @param start start index
     * @param before char count before change
     * @param count char count after change
     */
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes before they happen
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes after they happen
     */
    override fun afterTextChanged(editable: Editable) {
        val s = editable.toString()
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

    /**
     * Function that that handles the pay button and field errors
     * @param valid boolean that determines if the account number is valid
     */
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