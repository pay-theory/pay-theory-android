package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Variable to hold validation for cash contact information
 */
var cashContactFieldValid: Boolean = false
/**
 * Variable to hold validation for all cash fields
 */
var cashFieldsValid: Boolean = false
/**
 * Variable to hold validation for cash name
 */
var cashNameFieldValid: Boolean = false

/**
 * Function to check if all cash fields are valid
 * @param button pay theory button
 */
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
 * @param pt pay theory edit text
 * @param fragment pay theory fragment
 * @param submitButton pay theory button
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
            ptFragment!!.cash.contactInformation.setEmpty(true)
            ptFragment!!.cash.contactInformation.setValid(false)
            return
        }
        val isValid = isValidEmail(s.toString())

        ptFragment!!.cash.contactInformation.setEmpty(false)
        ptFragment!!.cash.contactInformation.setValid(isValid)
        handleButton(isValid)
    }

    /**
     * Function to check if email or phone is valid
     * @param target email or phone
     * @return Boolean
     */
    private fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches() || Patterns.PHONE.matcher(target).matches()
        }
    }

    /**
     * Function to handle button state
     * @param valid validation of field
     */
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
 * @param pt pay theory edit text
 * @param fragment pay theory fragment
 * @param submitButton pay theory button
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
            ptFragment!!.cash.payerName.setEmpty(true)
            ptFragment!!.cash.payerName.setValid(false)
            return
        }
        val isValid = isValid(s.toString())
        ptFragment!!.cash.payerName.setEmpty(false)
        ptFragment!!.cash.payerName.setValid(isValid)
        handleButton(isValid)
    }

    /**
     * Function to check if name is valid
     * @param target name
     * @return Boolean
     */
    private fun isValid(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target)
    }

    /**
     * Function to handle button state
     * @param valid validation of field
     */
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