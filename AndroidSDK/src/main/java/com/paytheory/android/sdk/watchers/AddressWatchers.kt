package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton


/**
 * Boolean that tracks the validity of the address field
 */
var addressValid: Boolean = false
/**
 * Boolean that tracks the validity of the city field
 */
var cityValid: Boolean = false
/**
 * Boolean that tracks the validity of the region code field (state)
 */
var regionValid: Boolean = false

/**
 * Boolean that tracks the validity of all bank fields
 */
var addressFieldsValid: Boolean = false

/**
 * * Function that checks the validity of all bank fields and enables/disables the pay button
 */
private fun areFieldsValid(button: PayTheoryButton, fragment: PayTheoryFragment?) {
    if (isAddressValid(button,fragment) == true && checkPaymentFieldValidity(fragment) == true){
        button.enable()
    } else {
        button.disable()
    }
}

fun checkPaymentFieldValidity(fragment: PayTheoryFragment?): Boolean {
    return isCardValid(fragment) && isBankValid(fragment) && isBankValid(fragment)
}

/**
 * Function that checks the validity of all bank fields and enables/disables the pay button
 * @param button pay theory button
 */
fun isAddressValid(button: PayTheoryButton, fragment: PayTheoryFragment?): Boolean{
    if (fragment?.requiresAddress() == false) {
        return true
    }
    //check if all card fields are valid
    addressFieldsValid = addressValid && cityValid && regionValid
    return addressFieldsValid
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AddressLine1TextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: EditText? = pt
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
            ptFragment!!.address.addressLine1.setEmpty(true)
            ptFragment!!.address.addressLine1.setValid(false)
            handleButton(false)
            return
        }

        ptFragment!!.address.addressLine1.setEmpty(false)
        ptFragment!!.address.addressLine1.setValid(true)
        handleButton(true)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            addressValid = false
            ptText!!.error = "Invalid Address"
        } else {
            addressValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }

}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CityTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: EditText? = pt
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
            ptFragment!!.address.city.setEmpty(true)
            ptFragment!!.address.city.setValid(false)
            handleButton(false)
        }

        cityValid = true
        ptFragment!!.address.city.setEmpty(false)
        ptFragment!!.address.city.setValid(true)
        handleButton(true)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            addressValid = false
            ptText!!.error = "Invalid City"
        } else {
            addressValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }

}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RegionTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: EditText? = pt
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
            ptFragment!!.address.region.setEmpty(true)
            ptFragment!!.address.region.setValid(false)
            handleButton(false)
        }

        regionValid = true
        val isValidLength = s.toString().length == 2
        ptFragment!!.address.region.setEmpty(false)
        ptFragment!!.address.region.setValid(isValidLength)
        handleButton(isValidLength)
    }
    private fun handleButton(valid: Boolean){
        if (!valid) {
            addressValid = false
            ptText!!.error = "Invalid Region"
        } else {
            addressValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }
}