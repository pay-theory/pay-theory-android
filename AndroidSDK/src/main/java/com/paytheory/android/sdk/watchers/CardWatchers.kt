package com.paytheory.android.sdk.watchers

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Boolean to track card number field validation
 */
var cardFieldValid: Boolean = false
/**
 * Boolean to track expiration field validation
 */
var expFieldValid: Boolean = false
/**
 * Boolean to track cvv field validation
 */
var cvvFieldValid: Boolean = false
/**
 * Boolean to track zip code field validation
 */
var zipCodeFieldValid: Boolean = false
/**
 * Boolean to track all card field validation
 */
var cardFieldsValid: Boolean = false

/**
 * Function to enable or disable submit button based on all card field validation
 * @param button pay theory button
 */
private fun areFieldsValid(button: PayTheoryButton){
    cardFieldsValid = cardFieldValid && expFieldValid && cvvFieldValid && zipCodeFieldValid
    if (cardFieldsValid){
        button.enable()
    } else {
        button.disable()
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CardNumberTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt
    private var ptFragment: PayTheoryFragment? = fragment
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.cardNumber.setEmpty(true)
            ptFragment!!.card.cardNumber.setValid(false)
            return
        }
        var changes = arrayOf(5,5,5,5)
        var maxLength = 23

        if (s.toString()[0] == '3') {
            changes = arrayOf(7,5)
            maxLength = 17
        }

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        var changeIndex = 0
        var stringIndex = 4

        while (stringIndex < s.length) {
            if (s.toString()[stringIndex] != ' ') {
                s.insert(stringIndex, " ")
                changeIndex += 1
            }

            if (changes.size > changeIndex) {
                stringIndex += changes[changeIndex]
            } else {
                stringIndex = s.length
            }
        }

        lock = false
        val isValidNumber = validLuhn(s.toString())
        ptFragment!!.card.cardNumber.setEmpty(false)
        ptFragment!!.card.cardNumber.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    /**
     * Function to check if card number is valid using Luhn algorithm
     * @param number card number string
     */
    private fun validLuhn(number: String): Boolean {
        val (digits, others) = number
            .filterNot(Char::isWhitespace)
            .partition(Char::isDigit)

        if (digits.length <= 1 || others.isNotEmpty()) {
            return false
        }

        val checksum = digits
            .map { it.code - '0'.code }
            .reversed()
            .mapIndexed { index, value ->
                if (index % 2 == 1 && value < 9) value * 2 % 9 else value
            }
            .sum()

        return checksum % 10 == 0
    }

    /**
     * Function to handle button state based on card number validation
     * @param valid boolean for validity of card number
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            cardFieldValid = false
            ptText!!.error = "Invalid Card Number"
        } else {
            cardFieldValid = true
        }
        areFieldsValid(submitButton)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class ExpirationTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt
    private var isDelete = false
    private var ptFragment: PayTheoryFragment? = fragment

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        isDelete = before != 0
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.expirationDate.setEmpty(true)
            ptFragment!!.card.expirationDate.setValid(false)
            return
        }

        val maxLength = 5

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        lock = false

        val isValidNumber = validExp(s.toString())
        ptFragment!!.card.expirationDate.setEmpty(false)
        ptFragment!!.card.expirationDate.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    /**
     * Function to check if expiration date is valid
     * @param number expiration date string
     */
    @SuppressLint("SimpleDateFormat")
    private fun validExp(number: String): Boolean {
        val length = number.length
        val stringBuilder = StringBuilder()
        stringBuilder.append(number)
        if (length > 0 && length == 3) {
            if (isDelete) stringBuilder.deleteCharAt(length - 1) else stringBuilder.insert(
                length - 1,
                "/"
            )
            ptText!!.setText(stringBuilder)
            ptText!!.setSelection(ptText!!.text!!.length)
        }

        //get current two digit year
        val currentTwoDigitYear: Int = SimpleDateFormat("yy").format(Calendar.getInstance().time).toInt()
        //get month value and check
        val currentText = ptText!!.text.toString()
        return (currentText.length == 5) && (currentText.substringBefore("/").toInt() < 13) && (currentText.substringBefore("/").toInt() != 0) && (currentText.substringAfter("/").toInt() >= currentTwoDigitYear)
    }

    /**
     * Function to handle button state based on expiration date validation
     * @param valid boolean for validity of expiration date
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            expFieldValid = false
            ptText!!.error = "Invalid Expiration"
        } else {
            expFieldValid = true
        }
        areFieldsValid(submitButton)
    }
}


/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CVVTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt
    private var ptFragment: PayTheoryFragment? = fragment

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.expirationDate.setEmpty(true)
            ptFragment!!.card.expirationDate.setValid(false)
            return
        }

        val maxLength = 4

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        lock = false
        val isValidNumber = validCVV(s.toString())
        ptFragment!!.card.cvv.setEmpty(false)
        ptFragment!!.card.cvv.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    /**
     * Function to check if cvv is valid
     * @param number cvv string
     */
    private fun validCVV(number: String): Boolean {
        val (digits, _) = number
            .partition(Char::isDigit)

        return !(digits.length < 3 || digits.length > 4)
    }

    /**
     * Function to handle button state based on cvv validation
     * @param valid boolean for validity of cvv
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            cvvFieldValid = false
            ptText!!.error = "Invalid CVV"
        } else {
            cvvFieldValid = true
        }
        areFieldsValid(submitButton)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class ZipCodeTextWatcher(pt: PayTheoryEditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt
    private var ptFragment: PayTheoryFragment? = fragment

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.postalCode.setEmpty(true)
            ptFragment!!.card.postalCode.setValid(false)
            return
        }

        val maxLength = 5

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        lock = false
        val isValidNumber = validZip(s.toString())
        ptFragment!!.card.postalCode.setEmpty(false)
        ptFragment!!.card.postalCode.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    /**
     * Function to check if zip code is valid
     * @param number zip code string
     */
    private fun validZip(number: String): Boolean {
        val (digits, _) = number
            .partition(Char::isDigit)

        return digits.length == 5
    }

    /**
     * Function to handle button state based on zip code validation
     * @param valid boolean for validity of zip code
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            zipCodeFieldValid = false
            ptText!!.error = "Invalid ZIP Code"
        } else {
            zipCodeFieldValid = true
        }
        areFieldsValid(submitButton)
    }
}