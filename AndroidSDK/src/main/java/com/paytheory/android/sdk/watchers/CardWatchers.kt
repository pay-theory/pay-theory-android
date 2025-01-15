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
    private var ptFragment: PayTheoryFragment? = fragment

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no-op
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // no-op
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.expirationDate.setEmpty(true)
            ptFragment!!.card.expirationDate.setValid(false)
            return
        }

        lock = true

        // Format the date and update the field
        val formatted = formatExpirationDate(s.toString())
        if (formatted != s.toString()) {
            s.replace(0, s.length, formatted)
            ptText!!.setSelection(formatted.length)
        }

        lock = false

        // Validate the formatted date
        val isValidNumber = validExp(formatted)
        ptFragment!!.card.expirationDate.setEmpty(false)
        ptFragment!!.card.expirationDate.setValid(isValidNumber)
        handleButton(isValidNumber)
    }

    private fun formatExpirationDate(input: String): String {
        // Remove all non-numeric characters
        val cleaned = input.filter { it.isDigit() }
        var formatted = ""

        if (cleaned.isNotEmpty()) {
            when {
                // If first digit is > 1, prepend 0 and treat first digit as start of year
                cleaned.first().toString().toInt() > 1 -> {
                    formatted = "0${cleaned.first()}/"
                    if (cleaned.length > 1) {
                        formatted += cleaned.substring(1, minOf(cleaned.length, 3))
                    }
                }
                // If first digit is 1, look at second digit
                cleaned.first().toString().toInt() == 1 -> {
                    if (cleaned.length > 1) {
                        // If second digit makes month > 12, format as 01/rest
                        if (cleaned[1].toString().toInt() > 2) {
                            formatted = "01/${cleaned.substring(1, minOf(cleaned.length, 3))}"
                        } else {
                            // Valid month starting with 1, format normally
                            formatted = cleaned.take(2)
                            if (cleaned.length > 2) {
                                formatted += "/${cleaned.substring(2, minOf(cleaned.length, 4))}"
                            }
                        }
                    } else {
                        // Just first digit entered
                        formatted = cleaned
                    }
                }
                // If first digit is 0, format normally
                else -> {
                    formatted = cleaned.take(2)
                    if (cleaned.length > 2) {
                        formatted += "/${cleaned.substring(2, minOf(cleaned.length, 4))}"
                    }
                }
            }
        }

        return formatted
    }

    @SuppressLint("SimpleDateFormat")
    private fun validExp(formattedDate: String): Boolean {
        // Don't validate incomplete dates
        if (!formattedDate.contains("/")) return true
        if (formattedDate.endsWith("/")) return true

        val month = formattedDate.substringBefore("/").toIntOrNull() ?: return false
        val year = formattedDate.substringAfter("/").toIntOrNull() ?: return false

        // Get current date components
        val calendar = Calendar.getInstance()
        val currentYear = SimpleDateFormat("yy").format(calendar.time).toInt()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based

        return when {
            month < 1 || month > 12 -> false
            year < currentYear -> false
            year == currentYear && month < currentMonth -> false
            else -> true
        }
    }

    private fun handleButton(valid: Boolean) {
        if (!valid) {
            expFieldValid = false
            ptText!!.error = "Invalid Expiration"
        } else {
            expFieldValid = true
            ptText!!.error = null
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