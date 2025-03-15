package com.paytheory.lib.valid

import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper

/**
 * `Validator` class provides utility functions for validating various types of input data.
 *
 * This class offers a set of static methods to perform common validation checks on
 * sensitive financial data like bank account numbers, routing numbers, card numbers,
 * expiration dates, CVCs, and postal codes, as well as general checks for non-empty strings.
 */
class Validator {

    /**
     * Performs a Luhn check on a secure string, typically used to validate credit card numbers.
     *
     * The Luhn algorithm, also known as the modulus 10 or mod 10 algorithm, is a simple checksum
     * formula used to validate a variety of identification numbers. This function applies the
     * algorithm to the digits of a secure string after revealing it for processing.
     *
     * @param secureString The [SecureString] containing the digits to be validated.
     * @return `true` if the secure string passes the Luhn check; `false` otherwise.
     */
    private fun luhnCheck(secureString: SecureStringWrapper): Boolean {
        val sanitized = secureString.secureValue.revealForUi().toString().replace("\\D".toRegex(), "")
        val sum = sanitized
            .map { it.toString().toInt() }
            .reversed()
            .mapIndexed { index, digit ->
                if (index % 2 == 1) {
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else {
                    digit
                }
            }.sum()

        return sum % 10 == 0
    }

    /**
     * Performs a routing number check on a secure string, commonly used to validate bank routing numbers.
     *
     * The routing number check involves a specific weighted sum calculation based on the position
     * of each digit in the routing number. This function applies the necessary calculations and
     * checks if the result is divisible by 10, which indicates a valid routing number.
     *
     * @param secureString The [SecureString] containing the digits of the routing number.
     * @return `true` if the secure string passes the routing number check; `false` otherwise.
     */
    private fun routingCheck(secureString: SecureStringWrapper): Boolean {
        val sanitized = secureString.secureValue.revealForUi().toString().replace("\\D".toRegex(), "")
        val sum = sanitized
            .map { it.toString().toInt() }
            .reversed()
            .mapIndexed { index, digit ->
                if ((sanitized.length - index) % 3 == 0) {
                    digit
                } else if ((sanitized.length - index) % 3 == 2) {
                    digit * 7
                } else {
                    digit * 3
                }
            }.sum()
        return sum % 10 == 0
    }

    /**
     * Validates a bank account number stored in a [SecureString].
     *
     * This function checks if the secure string, when revealed, contains between 7 and 17 digits
     * (inclusive), which is a common range for bank account numbers.
     *
     * @param secureString The [SecureString] containing the bank account number.
     * @return `true` if the secure string is a valid bank account number; `false` otherwise.
     */
    fun isValidBankAccountNumber(secureString: SecureStringWrapper): Boolean {
        return secureString.secureValue.revealForUi().filter { it.isDigit() }.length in 7..17
    }

    /**
     * Validates a bank routing number stored in a [SecureString].
     *
     * This function checks if the secure string contains exactly 9 digits and if it passes
     * the routing number check using the [routingCheck] method.
     *
     * @param secureString The [SecureString] containing the bank routing number.
     * @return `true` if the secure string is a valid bank routing number; `false` otherwise.
     */
    fun isValidBankRoutingNumber(secureString: SecureStringWrapper): Boolean {
        return secureString.secureValue.revealForUi().filter { it.isDigit() }.let { raw ->
            raw.toString().length == 9 && routingCheck(secureString)
        }
    }

    /**
     * Validates a credit card number stored in a [SecureString].
     *
     * This function checks if the secure string contains between 15 and 16 digits (inclusive)
     * and if it passes the Luhn check using the [luhnCheck] method.
     *
     * @param secureString The [SecureString] containing the credit card number.
     * @return `true` if the secure string is a valid credit card number; `false` otherwise.
     */
    fun isValidCardNumber(secureString: SecureStringWrapper): Boolean {
        return secureString.secureValue.revealForUi().filter { it.isDigit() }.let { raw ->
            raw.toString().length in 15..16 && luhnCheck(secureString)
        }
    }

    /**
     * Validates a credit card expiration date stored in a [SecureStringWrapper].
     *
     * This function checks if the expiration date, when revealed, matches the format MM/YY,
     * where MM is a two-digit month (01-12) and YY is a two-digit year.
     *
     * @param expiration The [SecureStringWrapper] containing the credit card expiration date.
     * @return `true` if the expiration date is in the correct format; `false` otherwise.
     */
    fun isValidExpiration(expiration: SecureStringWrapper): Boolean {
        return expiration.secureValue.revealForUi()
            .matches(Regex("^(0[1-9]|1[0-2])/(\\d{2})$"))
    }

    /**
     * Validates a CVC (Card Verification Code) stored in a [SecureString].
     *
     * This function checks if the secure string, when revealed, contains at least 3 digits,
     * which is the minimum length for CVCs.
     *
     * @param secureString The [SecureString] containing the CVC.
     * @return `true` if the CVC has a valid length; `false` otherwise.
     */
    fun isValidCvc(secureString: SecureStringWrapper): Boolean {
        return secureString.secureValue.revealForUi().length >= 3
    }

    /**
     * Validates a postal code stored in a [SecureString].
     *
     * This function checks if the secure string, when revealed, contains between 5 and 6
     * characters (inclusive), which is a common range for postal codes in certain regions.
     *
     * @param secureString The [SecureString] containing the postal code.
     * @return `true` if the postal code is within the valid length range; `false` otherwise.
     */
    fun isValidPostalCode(secureString: SecureStringWrapper): Boolean =
        secureString.secureValue.revealForUi().toString().length >= 5
                && secureString.secureValue.revealForUi().toString().length <= 6

    /**
     * Checks if a [SecureString] is not empty when revealed.
     *
     * This function provides a simple way to verify if a secure string contains any characters
     * after being revealed for UI display or validation purposes.
     *
     * @param secureString The [SecureString] to check for emptiness.
     * @return `true` if the secure string is not empty; `false` if it is empty.
     */
    fun isNotEmpty(secureString: SecureStringWrapper): Boolean = secureString.secureValue.revealForUi().isNotEmpty()
}