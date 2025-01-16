package com.paytheory.android.sdk.watchers

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Boolean that tracks the validity of the account name
 */
var cardNameValid: Boolean = false
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
 */
private fun areFieldsValid(button: PayTheoryButton, fragment: PayTheoryFragment?) {
    cardFieldsValid = cardNameValid && cardFieldValid && expFieldValid && cvvFieldValid && zipCodeFieldValid
    if (cardFieldsValid && isAddressValid(button,fragment) == true){
        button.enable()
    } else {
        button.disable()
    }

}

fun isCardValid(fragment: PayTheoryFragment?): Boolean {
    if (fragment?.chosenPaymentMethod() == PaymentMethodType.CARD) {
        return cardFieldsValid
    }
    return true
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CardNameTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) :
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
     * @param s editable text
     * @param start start index
     * @param count char count before change
     * @param after char count after change
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes after they happen
     * @param editable editable text
     */
    override fun afterTextChanged(editable: Editable) {
        val s = editable.toString()
        if (s.isEmpty()) {
            ptFragment!!.card.cardName.setEmpty(true)
            ptFragment!!.card.cardName.setValid(false)
            return
        }

        val isValidLength = s.toString().isNotEmpty()
        ptFragment!!.card.cardName.setEmpty(false)
        ptFragment!!.card.cardName.setValid(isValidLength)
        handleButton(isValidLength)
    }

    /**
     * Function that that handles the pay button and field errors
     * @param valid boolean that determines if the account number is valid
     */
    private fun handleButton(valid: Boolean){
        if (!valid) {
            cardNameValid = false
            ptText!!.error = "Invalid Account Name"
        } else {
            cardNameValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CardNumberTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
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
     * @param s editable text
     * @param start start index
     * @param count char count before change
     * @param after char count after change
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }
    /**
     * Function that handles text changes after they happen
     * @param s editable text
     */
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
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class ExpirationTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
    private var ptText: EditText? = pt
    private var isDelete = false
    private var ptFragment: PayTheoryFragment? = fragment

    /**
     * Function that handles text changes before they happen
     * @param s editable text
     * @param start start index
     * @param count char count before change
     * @param after char count after change
     */
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    /**
     * Function that handles text changes
     * @param s editable text
     * @param start start index
     * @param before char count before change
     * @param count char count after change
     */
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        isDelete = before != 0
    }

    /**
     * Function that handles text changes after they happen
     * @param s editable text
     */
    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.expirationDate.setEmpty(true)
            ptFragment!!.card.expirationDate.setValid(false)
            handleButton(false)
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
        var length = number.length
        val stringBuilder = StringBuilder()

        // Check first digit or first two digits for validity
        var checkBit = number.toString().first().toString()
        if (number.toString().length > 1) {
            checkBit = number.toString().subSequence(0,2).toString()
        }

        // If the check bit doesn't start with '0' and is greater than 1 or 12, prepend '0'
        if (checkBit.first() != '0') {
            if ((Integer.parseInt(checkBit) > 1 && Integer.parseInt(checkBit) < 10) ||
                Integer.parseInt(checkBit) > 12) {
                stringBuilder.append("0")
                length = length + 1
            }
        }

        // Append the original number to the StringBuilder
        stringBuilder.append(number)

        // If the length is 3, either delete the last char (if deleting) or insert '/'
        if (length > 0 && length == 3) {
            if (isDelete) stringBuilder.deleteCharAt(length - 1) else stringBuilder.insert(
                length - 1,
                "/"
            )
            ptText!!.setText(stringBuilder) // Update the EditText with the modified string
            ptText!!.setSelection(ptText!!.text!!.length) // Move cursor to the end
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
        areFieldsValid(submitButton,ptFragment)
    }
}


/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CVVTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
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
     * @param s editable text
     * @param start start index
     * @param count char count before change
     * @param after char count after change
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }
    /**
     * Function that handles text changes after they happen
     * @param s editable text
     */
    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.expirationDate.setEmpty(true)
            ptFragment!!.card.expirationDate.setValid(false)
            handleButton(false)
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
        areFieldsValid(submitButton,ptFragment)
    }
}

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class ZipCodeTextWatcher(pt: EditText, fragment: PayTheoryFragment, private var submitButton: PayTheoryButton) : TextWatcher {
    private var lock = false
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
     * @param s editable text
     * @param start start index
     * @param count char count before change
     * @param after char count after change
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }
    /**
     * Function that handles text changes after they happen
     * @param s editable text
     */
    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            ptFragment!!.card.postalCode.setEmpty(true)
            ptFragment!!.card.postalCode.setValid(false)
            handleButton(false)
            return
        }

        val maxLength = 6

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        ptText!!.setText(s.toString().replace(Regex("[^a-zA-Z0-9]"), "").uppercase(Locale.getDefault()))
        ptText!!.setSelection(ptText!!.text!!.length) // Move cursor to the end

        lock = false
        val isValid = validZip(s.toString())
        ptFragment!!.card.postalCode.setEmpty(false)
        ptFragment!!.card.postalCode.setValid(isValid)
        handleButton(isValid)
    }

    /**
     * Function to check if zip code is valid
     * @param number zip code string
     */
    private fun validZip(code: String): Boolean {
        if (code.length < 5 || code.length > 6) return false
        return true
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
            ptText!!.error = null
            zipCodeFieldValid = true
        }
        areFieldsValid(submitButton,ptFragment)
    }
}