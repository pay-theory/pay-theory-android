package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton


/**
 * Boolean that tracks the validity of the account type
 */
var accountTypeValid: Boolean = false
/**
 * Boolean that tracks the validity of the account name
 */
var accountNameValid: Boolean = false
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
 */
private fun areFieldsValid(button: PayTheoryButton, fragment: PayTheoryFragment?) {
    //check if all card fields are valid
    bankFieldsValid = accountNumberValid && routingNumberValid && accountNameValid && accountTypeValid
    //if all card fields are valid enable
    if (bankFieldsValid && isAddressValid(button,fragment) == true){
        button.enable()
    } else {
        button.disable()
    }
}

fun isBankValid(fragment: PayTheoryFragment?): Boolean{
    if (fragment?.chosenPaymentMethod() == PaymentMethodType.BANK) {
        return addressFieldsValid
    }
    return true
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RoutingNumberTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
            ptFragment!!.ach.routingNumber.setEmpty(true)
            ptFragment!!.ach.routingNumber.setValid(false)
            handleButton(false)
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
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AccountNumberTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
            ptFragment!!.ach.accountNumber.setEmpty(true)
            ptFragment!!.ach.accountNumber.setValid(false)
            handleButton(false)
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
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AccountNameTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
            ptFragment!!.ach.accountName.setEmpty(true)
            ptFragment!!.ach.accountName.setValid(false)
            handleButton(false)
            return
        }

        val isValidLength = s.toString().isNotEmpty()
        ptFragment!!.ach.accountName.setEmpty(false)
        ptFragment!!.ach.accountName.setValid(isValidLength)
        handleButton(isValidLength)
    }

    /**
     * Function that that handles the pay button and field errors
     * @param valid boolean that determines if the account number is valid
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            accountNameValid = false
            ptText!!.error = "Invalid Account Name"
        } else {
            accountNameValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AccountTypeTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
            ptFragment!!.ach.accountName.setEmpty(true)
            ptFragment!!.ach.accountName.setValid(false)
            handleButton(false)
            return
        }

        val isValid = listOf(
            ptFragment?.context?.getString(R.string.checking),
            ptFragment?.context?.getString(R.string.savings)).contains(s.toString())
        ptFragment!!.ach.accountName.setEmpty(false)
        ptFragment!!.ach.accountName.setValid(isValid)
        handleButton(isValid)
    }

    /**
     * Function that that handles the pay button and field errors
     * @param valid boolean that determines if the account number is valid
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            accountTypeValid = false
            ptText!!.error = "Invalid Account Type"
        } else {
            accountTypeValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }
}