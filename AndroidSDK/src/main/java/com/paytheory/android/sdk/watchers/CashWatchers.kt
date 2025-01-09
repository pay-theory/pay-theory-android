package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText

var cashContactFieldValid: Boolean = false
var cashFieldsValid: Boolean = false
var cashNameFieldValid: Boolean = false

/*
* Modernization
* Watchers have been updated to support ValidAndEmpty protocol
* changes are in afterTextChanged
* */

private fun areFieldsValid(button: PayTheoryButton){
    //check if all card fields are valid
    cashFieldsValid = cashContactFieldValid && cashNameFieldValid
    //if all card fields are valid enable
    if (cashFieldsValid){
        button.enable()
    } else {
        button.disable()
    }
}
/**
 * Text watcher class to validate buyer contact edit text field
 */
class CashContactTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
        val isValid = isValidEmail(s.toString())

        ptFragment!!.ach.routingNumber.setEmpty(false)
        ptFragment!!.ach.routingNumber.setValid(isValid)
        handleButton(isValid)
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches() || Patterns.PHONE.matcher(target).matches()
        }
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            cashContactFieldValid = false
            ptText!!.error = "Invalid Contact"
        } else {
            cashContactFieldValid = true
        }
        areFieldsValid(submitButton)
    }

}


/**
 * Text watcher class to validate buyer contact edit text field
 */
class CashNameTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
        val isValid = isValid(s.toString())
        ptFragment!!.ach.routingNumber.setEmpty(false)
        ptFragment!!.ach.routingNumber.setValid(isValid)
        handleButton(isValid)
    }

    private fun isValid(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            cashNameFieldValid = false
            ptText!!.error = "Invalid Name"
        } else {
            cashNameFieldValid = true
        }
        areFieldsValid(submitButton)
    }

}